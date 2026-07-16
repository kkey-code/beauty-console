package com.wkr.storeserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.PaymentRecordDTO;
import com.wkr.storepojo.dto.PaymentRecordPageQueryDTO;
import com.wkr.storepojo.entity.PaymentRecord;
import com.wkr.storepojo.vo.PaymentRecordVO;
import com.wkr.storeserver.audit.AuditLog;
import com.wkr.storeserver.service.PaymentRecordService;
import com.wkr.storeserver.service.ServiceOrderService;
import com.wkr.storeserver.support.DataScopeSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 收款流水接口控制器，负责接收管理端请求、校验参数并调用服务层完成业务处理。
 */
@Slf4j
@RestController
@RequestMapping("/admin/payment-records")
@Tag(name = "收款流水相关接口")
public class PaymentRecordController {

    private final PaymentRecordService paymentRecordService;
    private final ServiceOrderService serviceOrderService;

    public PaymentRecordController(
            PaymentRecordService paymentRecordService,
            ServiceOrderService serviceOrderService) {
        this.paymentRecordService = paymentRecordService;
        this.serviceOrderService = serviceOrderService;
    }

    @GetMapping
    @Operation(summary = "获取收款流水列表")
    public Result<PageResult<PaymentRecordVO>> getList(@Valid PaymentRecordPageQueryDTO dto) {
        Page<PaymentRecord> page = new Page<>(dto.getPage(), dto.getPageSize());

        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getOrderId() != null, PaymentRecord::getOrderId, dto.getOrderId())
                .eq(StringUtils.hasText(dto.getPaymentMethod()), PaymentRecord::getPaymentMethod, dto.getPaymentMethod())
                .eq(dto.getPayStatus() != null, PaymentRecord::getPayStatus, dto.getPayStatus())
                .ge(dto.getBeginTime() != null, PaymentRecord::getPayTime, dto.getBeginTime())
                .le(dto.getEndTime() != null, PaymentRecord::getPayTime, dto.getEndTime())
                .orderByDesc(PaymentRecord::getPayTime)
                .orderByDesc(PaymentRecord::getId);
        applyDataScope(wrapper);

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
    @Operation(summary = "收款流水详情")
    public Result<PaymentRecordVO> get(@PathVariable("id") Long id) {
        PaymentRecord record = paymentRecordService.getById(id);
        if (record == null) {
            throw new BusinessException("收款流水不存在");
        }
        serviceOrderService.assertCanAccess(record.getOrderId());
        return Result.success(toVO(record));
    }

    @PostMapping
    @Operation(summary = "新增收款记录")
    @AuditLog(action = "CREATE", target = "PAYMENT_RECORD")
    public Result<Long> save(@Valid @RequestBody PaymentRecordDTO dto) {
        serviceOrderService.assertCanAccess(dto.getOrderId());
        return Result.success(paymentRecordService.createPaymentRecord(dto));
    }

    @PatchMapping("/{id}/void")
    @Operation(summary = "作废收款记录")
    @AuditLog(action = "VOID", target = "PAYMENT_RECORD")
    public Result<Boolean> voidPaymentRecord(@PathVariable("id") Long id) {
        PaymentRecord record = paymentRecordService.getById(id);
        if (record == null) {
            throw new BusinessException("收款流水不存在");
        }
        serviceOrderService.assertCanAccess(record.getOrderId());
        return Result.success(paymentRecordService.voidPaymentRecord(id));
    }

    private void applyDataScope(LambdaQueryWrapper<PaymentRecord> wrapper) {
        Long staffId = DataScopeSupport.currentScopedStaffId();
        if (staffId == null) {
            return;
        }
        wrapper.and(scope -> scope
                .exists("SELECT 1 FROM service_order_item soi_scope "
                        + "WHERE soi_scope.order_id = payment_record.order_id AND soi_scope.staff_id = {0}", staffId)
                .or()
                .exists("SELECT 1 FROM service_order so_scope "
                        + "JOIN appointment a_scope ON a_scope.id = so_scope.appointment_id "
                        + "WHERE so_scope.id = payment_record.order_id "
                        + "AND so_scope.deleted = 0 AND a_scope.deleted = 0 AND a_scope.staff_id = {0}", staffId));
    }

    private PaymentRecordVO toVO(PaymentRecord record) {
        PaymentRecordVO vo = new PaymentRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }
}
