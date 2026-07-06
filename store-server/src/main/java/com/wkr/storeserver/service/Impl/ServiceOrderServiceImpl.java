package com.wkr.storeserver.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storepojo.entity.PaymentRecord;
import com.wkr.storepojo.entity.ServiceOrder;
import com.wkr.storepojo.entity.ServiceOrderItem;
import com.wkr.storepojo.entity.StaffMember;
import com.wkr.storepojo.vo.PaymentRecordVO;
import com.wkr.storepojo.vo.ServiceOrderItemVO;
import com.wkr.storeserver.service.PaymentRecordService;
import com.wkr.storeserver.service.ServiceOrderItemService;
import com.wkr.storeserver.service.ServiceOrderService;
import com.wkr.storeserver.service.StaffMemberService;
import com.wkr.storeserver.mapper.ServiceOrderMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author kkey
* @description 针对表【service_order(订单主表)】的数据库操作Service实现
* @createDate 2026-07-03 21:10:41
*/
@Service
public class ServiceOrderServiceImpl extends ServiceImpl<ServiceOrderMapper, ServiceOrder>
    implements ServiceOrderService{

    @Autowired
    private ServiceOrderItemService serviceOrderItemService;
    @Autowired
    private PaymentRecordService paymentRecordService;
    @Autowired
    private StaffMemberService staffMemberService;

    @Override
    public List<ServiceOrderItemVO> getOrderItemsByOrderId(Long orderId) {
        // 1. 查询该订单的所有明细
        LambdaQueryWrapper<ServiceOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceOrderItem::getOrderId, orderId);
        List<ServiceOrderItem> itemList = serviceOrderItemService.list(wrapper);

        // 2. 转换为 VO
        return itemList.stream()
                .map(item -> {
                    ServiceOrderItemVO itemVO = new ServiceOrderItemVO();
                    BeanUtils.copyProperties(item, itemVO);
                    if (item.getStaffId() != null) {
                        StaffMember staffMember = staffMemberService.getById(item.getStaffId());
                        if (staffMember != null) {
                            itemVO.setStaffName(staffMember.getName());
                        }
                    }
                    return itemVO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentRecordVO> getPaymentRecordByOrderId(Long orderId) {
        // 1. 查询该订单的所有明细
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRecord::getOrderId, orderId);
        List<PaymentRecord> paymentRecordList = paymentRecordService.list(wrapper);

        // 2. 转换为 VO
        return paymentRecordList.stream()
                .map(item -> {
                    PaymentRecordVO paymentRecordVO = new PaymentRecordVO();
                    BeanUtils.copyProperties(item, paymentRecordVO);
                    return paymentRecordVO;
                })
                .collect(Collectors.toList());
    }

}




