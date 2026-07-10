package com.wkr.storeserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.PaymentRecordDTO;
import com.wkr.storepojo.dto.PaymentRecordPageQueryDTO;
import com.wkr.storepojo.entity.PaymentRecord;
import com.wkr.storepojo.entity.ServiceOrder;
import com.wkr.storepojo.enums.DebtStatusEnum;
import com.wkr.storepojo.enums.OrderStatusEnum;
import com.wkr.storepojo.enums.PaymentRecordStatusEnum;
import com.wkr.storepojo.enums.PayStatusEnum;
import com.wkr.storepojo.vo.PaymentRecordVO;
import com.wkr.storeserver.service.PaymentRecordService;
import com.wkr.storeserver.service.ServiceOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 收款流水接口控制器，负责接收管理端请求、校验参数并调用服务层完成业务处理。
 */
@Slf4j
@RestController
@RequestMapping("/admin/payment-records")
@Api(tags = "收款流水相关接口")
public class PaymentRecordController {

    private static final DateTimeFormatter PAYMENT_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final PaymentRecordService paymentRecordService;
    private final ServiceOrderService serviceOrderService;

    public PaymentRecordController(
            PaymentRecordService paymentRecordService,
            ServiceOrderService serviceOrderService) {
        this.paymentRecordService = paymentRecordService;
        this.serviceOrderService = serviceOrderService;
    }

    @GetMapping
    @ApiOperation("获取收款流水列表")
    public Result<PageResult<PaymentRecordVO>> getList(PaymentRecordPageQueryDTO dto) {
        Page<PaymentRecord> page = new Page<>(dto.getPage(), dto.getPageSize());

        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getOrderId() != null, PaymentRecord::getOrderId, dto.getOrderId())
                .eq(StringUtils.hasText(dto.getPaymentMethod()), PaymentRecord::getPaymentMethod, dto.getPaymentMethod())
                .eq(dto.getPayStatus() != null, PaymentRecord::getPayStatus, dto.getPayStatus())
                .ge(dto.getBeginTime() != null, PaymentRecord::getPayTime, dto.getBeginTime())
                .le(dto.getEndTime() != null, PaymentRecord::getPayTime, dto.getEndTime())
                .orderByDesc(PaymentRecord::getPayTime)
                .orderByDesc(PaymentRecord::getId);

        Page<PaymentRecord> pageRecord = paymentRecordService.page(page, wrapper);

        List<PaymentRecordVO> voList = pageRecord.getRecords().stream()
                .map(this::toVO)
                .toList();

        return Result.success(PageResult.<PaymentRecordVO>builder()
                .total(pageRecord.getTotal())
                .records(voList)
                .build());
    }

    @GetMapping("/{id}")
    @ApiOperation("收款流水详情")
    public Result<PaymentRecordVO> get(@PathVariable("id") Long id) {
        PaymentRecord record = paymentRecordService.getById(id);
        if (record == null) {
            throw new BusinessException("收款流水不存在");
        }
        return Result.success(toVO(record));
    }

    @PostMapping
    @ApiOperation("新增收款记录")
    @Transactional
    public Result<Long> save(@Valid @RequestBody PaymentRecordDTO dto) {
        ServiceOrder order = getPayableOrder(dto.getOrderId());

        PaymentRecord record = new PaymentRecord();
        BeanUtils.copyProperties(dto, record);
        fillPaymentDefaults(record);
        checkPaymentNoDuplicate(record.getPaymentNo());

        boolean saved = paymentRecordService.save(record);
        if (!saved) {
            throw new BusinessException("新增收款记录失败");
        }

        // 只有成功收款或退款流水会影响订单金额；未确认流水只是先记录，不改订单。
        applyPaymentToOrder(order, record);
        return Result.success(record.getId());
    }

    @PatchMapping("/{id}/void")
    @ApiOperation("作废收款记录")
    @Transactional
    public Result<Boolean> voidPaymentRecord(@PathVariable("id") Long id) {
        PaymentRecord record = paymentRecordService.getById(id);
        if (record == null) {
            throw new BusinessException("收款流水不存在");
        }
        if (PaymentRecordStatusEnum.VOIDED.matches(record.getPayStatus())) {
            throw new BusinessException("收款流水已经作废，不能重复作废");
        }

        ServiceOrder order = getPayableOrder(record.getOrderId());

        // 作废成功收款要扣回已收金额；作废退款流水要把已收金额加回去。
        rollbackPaymentFromOrder(order, record);

        record.setPayStatus(PaymentRecordStatusEnum.VOIDED.getCode());
        boolean updated = paymentRecordService.updateById(record);
        return Result.success(updated);
    }

    private void fillPaymentDefaults(PaymentRecord record) {
        if (!StringUtils.hasText(record.getPaymentNo())) {
            record.setPaymentNo(generatePaymentNo());
        }
        if (record.getPayTime() == null) {
            record.setPayTime(LocalDateTime.now());
        }
        if (record.getOperatorId() == null) {
            record.setOperatorId(BaseContext.getCurrentId());
        }
        record.setCreateTime(LocalDateTime.now());
    }

    private ServiceOrder getPayableOrder(Long orderId) {
        ServiceOrder order = serviceOrderService.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (OrderStatusEnum.CANCELED.matches(order.getOrderStatus())) {
            throw new BusinessException("订单已取消，不能收款");
        }
        return order;
    }

    private void checkPaymentNoDuplicate(String paymentNo) {
        long count = paymentRecordService.count(new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getPaymentNo, paymentNo));
        if (count > 0) {
            throw new BusinessException("收款流水号已存在");
        }
    }

    private void applyPaymentToOrder(ServiceOrder order, PaymentRecord record) {
        if (PaymentRecordStatusEnum.UNCONFIRMED.matches(record.getPayStatus())) {
            return;
        }
        if (PaymentRecordStatusEnum.VOIDED.matches(record.getPayStatus())) {
            throw new BusinessException("新增收款记录不能直接作废");
        }

        BigDecimal payAmount = valueOrZero(record.getPayAmount());
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("收款金额必须大于0");
        }

        BigDecimal paidAmount = valueOrZero(order.getPaidAmount());
        BigDecimal receivableAmount = valueOrZero(order.getReceivableAmount());
        BigDecimal newPaidAmount;

        if (PaymentRecordStatusEnum.SUCCESS.matches(record.getPayStatus())) {
            newPaidAmount = paidAmount.add(payAmount);
            if (newPaidAmount.compareTo(receivableAmount) > 0) {
                throw new BusinessException("收款金额超过订单剩余应收金额");
            }
        } else if (PaymentRecordStatusEnum.REFUNDED.matches(record.getPayStatus())) {
            newPaidAmount = paidAmount.subtract(payAmount);
            if (newPaidAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("退款金额不能大于订单已收金额");
            }
        } else {
            throw new BusinessException("收款状态错误");
        }

        refreshOrderPaymentAmount(order, newPaidAmount);
    }

    private void rollbackPaymentFromOrder(ServiceOrder order, PaymentRecord record) {
        BigDecimal paidAmount = valueOrZero(order.getPaidAmount());
        BigDecimal payAmount = valueOrZero(record.getPayAmount());
        BigDecimal newPaidAmount = paidAmount;

        if (PaymentRecordStatusEnum.SUCCESS.matches(record.getPayStatus())) {
            newPaidAmount = paidAmount.subtract(payAmount);
            if (newPaidAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("作废后订单已收金额不能小于0");
            }
        } else if (PaymentRecordStatusEnum.REFUNDED.matches(record.getPayStatus())) {
            newPaidAmount = paidAmount.add(payAmount);
            if (newPaidAmount.compareTo(valueOrZero(order.getReceivableAmount())) > 0) {
                throw new BusinessException("作废退款后订单已收金额不能大于应收金额");
            }
        }

        refreshOrderPaymentAmount(order, newPaidAmount);
    }

    private void refreshOrderPaymentAmount(ServiceOrder order, BigDecimal newPaidAmount) {
        BigDecimal receivableAmount = valueOrZero(order.getReceivableAmount());
        BigDecimal newDebtAmount = receivableAmount.subtract(newPaidAmount);

        order.setPaidAmount(newPaidAmount);
        order.setDebtAmount(newDebtAmount);
        order.setPayStatus(payStatusOf(newPaidAmount, receivableAmount));
        order.setDebtStatus(newDebtAmount.compareTo(BigDecimal.ZERO) > 0
                ? DebtStatusEnum.INSTALLMENT.getCode()
                : DebtStatusEnum.NONE.getCode());
        order.setUpdateTime(LocalDateTime.now());

        boolean updated = serviceOrderService.updateById(order);
        if (!updated) {
            throw new BusinessException("更新订单收款金额失败");
        }
    }

    private Integer payStatusOf(BigDecimal paidAmount, BigDecimal receivableAmount) {
        if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            return PayStatusEnum.UNPAID.getCode();
        }
        if (paidAmount.compareTo(receivableAmount) < 0) {
            return PayStatusEnum.PARTIAL_PAID.getCode();
        }
        return PayStatusEnum.PAID.getCode();
    }

    private String generatePaymentNo() {
        return "PAY" + LocalDateTime.now().format(PAYMENT_NO_FORMATTER);
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private PaymentRecordVO toVO(PaymentRecord record) {
        PaymentRecordVO vo = new PaymentRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }
}
