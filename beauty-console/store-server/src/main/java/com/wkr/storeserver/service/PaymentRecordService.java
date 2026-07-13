package com.wkr.storeserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkr.storepojo.dto.PaymentRecordDTO;
import com.wkr.storepojo.entity.PaymentRecord;

/**
* @author kkey
* @description 针对表【payment_record(收款流水表)】的数据库操作Service
* @createDate 2026-07-03 21:10:41
*/
public interface PaymentRecordService extends IService<PaymentRecord> {

    Long createPaymentRecord(PaymentRecordDTO dto);

    boolean voidPaymentRecord(Long id);
}
