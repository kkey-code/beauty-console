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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "收款流水相关接口")
public class PaymentRecordController {

    private final PaymentRecordService paymentRecordService;

    public PaymentRecordController(PaymentRecordService paymentRecordService) {
        this.paymentRecordService = paymentRecordService;
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
    @AuditLog(action = "CREATE", target = "PAYMENT_RECORD")
    public Result<Long> save(@Valid @RequestBody PaymentRecordDTO dto) {
        return Result.success(paymentRecordService.createPaymentRecord(dto));
    }

    @PatchMapping("/{id}/void")
    @ApiOperation("作废收款记录")
    @AuditLog(action = "VOID", target = "PAYMENT_RECORD")
    public Result<Boolean> voidPaymentRecord(@PathVariable("id") Long id) {
        return Result.success(paymentRecordService.voidPaymentRecord(id));
    }

    private PaymentRecordVO toVO(PaymentRecord record) {
        PaymentRecordVO vo = new PaymentRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }
}
