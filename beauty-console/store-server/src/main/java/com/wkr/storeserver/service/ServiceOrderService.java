package com.wkr.storeserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storepojo.dto.ServiceOrderDTO;
import com.wkr.storepojo.dto.ServiceOrderPageQueryDTO;
import com.wkr.storepojo.entity.ServiceOrder;
import com.wkr.storepojo.vo.PaymentRecordVO;
import com.wkr.storepojo.vo.ServiceOrderItemVO;
import com.wkr.storepojo.vo.ServiceOrderVO;

import java.util.List;

/**
* @author kkey
* @description 针对表【service_order(订单主表)】的数据库操作Service
* @createDate 2026-07-03 21:10:41
*/
public interface ServiceOrderService extends IService<ServiceOrder> {

    PageResult<ServiceOrderVO> pageOrders(ServiceOrderPageQueryDTO dto);

    List<ServiceOrderVO> listPendingOrderSummaries(int limit);

    long countVisibleOrders();

    void assertCanAccess(Long id);

    ServiceOrderVO getDetail(Long id);

    Long createOrder(ServiceOrderDTO dto);

    Long createOrder(ServiceOrderDTO dto, String requestId);

    Long createFromAppointment(Long appointmentId);

    Long createFromAppointment(Long appointmentId, String requestId);

    boolean updateOrder(Long id, ServiceOrderDTO dto);

    boolean cancel(Long id);

    List<ServiceOrderItemVO> getOrderItemsByOrderId(Long orderId);

    List<PaymentRecordVO> getPaymentRecordByOrderId(Long id);

    boolean finish(Long id);

    boolean deleteOrder(Long id);
}
