package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storepojo.entity.PaymentRecord;
import com.wkr.storeserver.service.PaymentRecordService;
import com.wkr.storeserver.mapper.PaymentRecordMapper;
import org.springframework.stereotype.Service;

/**
* @author kkey
* @description 针对表【payment_record(收款流水表)】的数据库操作Service实现
* @createDate 2026-07-03 21:10:41
*/
@Service
public class PaymentRecordServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord>
    implements PaymentRecordService{

}




