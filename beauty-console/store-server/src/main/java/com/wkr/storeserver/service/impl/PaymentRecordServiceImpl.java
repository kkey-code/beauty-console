package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.PaymentRecordDTO;
import com.wkr.storepojo.entity.PaymentRecord;
import com.wkr.storepojo.entity.ServiceOrder;
import com.wkr.storepojo.enums.DebtStatusEnum;
import com.wkr.storepojo.enums.OrderStatusEnum;
import com.wkr.storepojo.enums.PaymentRecordStatusEnum;
import com.wkr.storepojo.enums.PayStatusEnum;
import com.wkr.storeserver.mapper.ServiceOrderMapper;
import com.wkr.storeserver.service.PaymentRecordService;
import com.wkr.storeserver.mapper.PaymentRecordMapper;
import com.wkr.storeserver.support.BusinessNoGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
* @author kkey
* @description 针对表【payment_record(收款流水表)】的数据库操作Service实现
* @createDate 2026-07-03 21:10:41
*/
@Service
public class PaymentRecordServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord>
    implements PaymentRecordService{

    private final PaymentRecordMapper paymentRecordMapper;
    private final ServiceOrderMapper serviceOrderMapper;

    public PaymentRecordServiceImpl(
            PaymentRecordMapper paymentRecordMapper,
            ServiceOrderMapper serviceOrderMapper) {
        this.paymentRecordMapper = paymentRecordMapper;
        this.serviceOrderMapper = serviceOrderMapper;
    }

    @Override
    @Transactional
    public Long createPaymentRecord(PaymentRecordDTO dto) {
        ServiceOrder order = getPayableOrderForUpdate(dto.getOrderId());

        PaymentRecord record = new PaymentRecord();
        BeanUtils.copyProperties(dto, record);
        fillPaymentDefaults(record);
        checkPaymentNoDuplicate(record.getPaymentNo());

        if (paymentRecordMapper.insert(record) != 1) {
            throw new BusinessException("新增收款记录失败");
        }

        applyPaymentToOrder(order, record);
        return record.getId();
    }

    @Override
    @Transactional
    public boolean voidPaymentRecord(Long id) {
        PaymentRecord record = paymentRecordMapper.selectOne(new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getId, id)
                .last("FOR UPDATE"));
        if (record == null) {
            throw new BusinessException("收款流水不存在");
        }
        if (PaymentRecordStatusEnum.VOIDED.matches(record.getPayStatus())) {
            throw new BusinessException("收款流水已经作废，不能重复作废");
        }

        ServiceOrder order = getPayableOrderForUpdate(record.getOrderId());
        rollbackPaymentFromOrder(order, record);

        record.setPayStatus(PaymentRecordStatusEnum.VOIDED.getCode());
        return paymentRecordMapper.updateById(record) == 1;
    }

    private void fillPaymentDefaults(PaymentRecord record) {
        if (!StringUtils.hasText(record.getPaymentNo())) {
            record.setPaymentNo(BusinessNoGenerator.next("PAY"));
        }
        if (record.getPayTime() == null) {
            record.setPayTime(LocalDateTime.now());
        }
        if (record.getOperatorId() == null) {
            record.setOperatorId(BaseContext.getCurrentId());
        }
        record.setCreateTime(LocalDateTime.now());
    }

    private ServiceOrder getPayableOrderForUpdate(Long orderId) {
        ServiceOrder order = serviceOrderMapper.selectOne(new LambdaQueryWrapper<ServiceOrder>()
                .eq(ServiceOrder::getId, orderId)
                .last("FOR UPDATE"));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (OrderStatusEnum.CANCELED.matches(order.getOrderStatus())) {
            throw new BusinessException("订单已取消，不能收款");
        }
        return order;
    }

    private void checkPaymentNoDuplicate(String paymentNo) {
        long count = paymentRecordMapper.selectCount(new LambdaQueryWrapper<PaymentRecord>()
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

        BigDecimal payAmount = positiveAmount(record.getPayAmount());
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

        if (serviceOrderMapper.updateById(order) != 1) {
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

    private BigDecimal positiveAmount(BigDecimal value) {
        BigDecimal amount = valueOrZero(value);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("收款金额必须大于0");
        }
        return amount;
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
