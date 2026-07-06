package com.wkr.storeserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkr.storepojo.entity.ServiceOrder;
import com.wkr.storepojo.vo.PaymentRecordVO;
import com.wkr.storepojo.vo.ServiceOrderItemVO;

import java.util.List;

/**
* @author kkey
* @description 针对表【service_order(订单主表)】的数据库操作Service
* @createDate 2026-07-03 21:10:41
*/
public interface ServiceOrderService extends IService<ServiceOrder> {

    List<ServiceOrderItemVO> getOrderItemsByOrderId(Long orderId);

    List<PaymentRecordVO> getPaymentRecordByOrderId(Long id);
}
