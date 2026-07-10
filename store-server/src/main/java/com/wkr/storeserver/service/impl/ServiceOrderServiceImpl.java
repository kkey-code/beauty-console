package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.InventoryStockLogDTO;
import com.wkr.storepojo.entity.PaymentRecord;
import com.wkr.storepojo.entity.ServiceOrder;
import com.wkr.storepojo.entity.ServiceOrderItem;
import com.wkr.storepojo.entity.ServiceProjectInventory;
import com.wkr.storepojo.entity.StaffMember;
import com.wkr.storepojo.enums.InventoryChangeTypeEnum;
import com.wkr.storepojo.enums.OrderStatusEnum;
import com.wkr.storepojo.enums.OrderTypeEnum;
import com.wkr.storepojo.vo.PaymentRecordVO;
import com.wkr.storepojo.vo.ServiceOrderItemVO;
import com.wkr.storeserver.mapper.ServiceOrderMapper;
import com.wkr.storeserver.service.InventoryStockLogService;
import com.wkr.storeserver.service.PaymentRecordService;
import com.wkr.storeserver.service.ServiceProjectInventoryService;
import com.wkr.storeserver.service.ServiceOrderItemService;
import com.wkr.storeserver.service.ServiceOrderService;
import com.wkr.storeserver.service.StaffMemberService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author kkey
* @description 针对表【service_order(订单主表)】的数据库操作Service实现
* @createDate 2026-07-03 21:10:41
*/
@Service
public class ServiceOrderServiceImpl extends ServiceImpl<ServiceOrderMapper, ServiceOrder>
    implements ServiceOrderService{

    private final ServiceOrderMapper serviceOrderMapper;
    private final ServiceOrderItemService serviceOrderItemService;
    private final PaymentRecordService paymentRecordService;
    private final StaffMemberService staffMemberService;
    private final ServiceProjectInventoryService serviceProjectInventoryService;
    private final InventoryStockLogService inventoryStockLogService;

    public ServiceOrderServiceImpl(
            ServiceOrderMapper serviceOrderMapper,
            ServiceOrderItemService serviceOrderItemService,
            PaymentRecordService paymentRecordService,
            StaffMemberService staffMemberService,
            ServiceProjectInventoryService serviceProjectInventoryService,
            InventoryStockLogService inventoryStockLogService) {
        this.serviceOrderMapper = serviceOrderMapper;
        this.serviceOrderItemService = serviceOrderItemService;
        this.paymentRecordService = paymentRecordService;
        this.staffMemberService = staffMemberService;
        this.serviceProjectInventoryService = serviceProjectInventoryService;
        this.inventoryStockLogService = inventoryStockLogService;
    }

    @Override
    public List<ServiceOrderItemVO> getOrderItemsByOrderId(Long orderId) {
        // 1. 查询该订单的所有明细
        LambdaQueryWrapper<ServiceOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceOrderItem::getOrderId, orderId);
        List<ServiceOrderItem> itemList = serviceOrderItemService.list(wrapper);
        Set<Long> staffIds = itemList.stream()
                .map(ServiceOrderItem::getStaffId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, String> staffNameById = loadStaffNames(staffIds);

        // 2. 转换为 VO
        return itemList.stream()
                .map(item -> {
                    ServiceOrderItemVO itemVO = new ServiceOrderItemVO();
                    BeanUtils.copyProperties(item, itemVO);
                    itemVO.setStaffName(staffNameById.get(item.getStaffId()));
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

    @Override
    @Transactional
    public boolean finish(Long id) {
        ServiceOrder serviceOrder = serviceOrderMapper.selectOne(
                new LambdaQueryWrapper<ServiceOrder>()
                        .eq(ServiceOrder::getId, id)
                        .last("FOR UPDATE"));
        if (serviceOrder == null) {
            throw new BusinessException("订单不存在");
        }

        OrderStatusEnum.validate(serviceOrder.getOrderStatus());
        if (OrderStatusEnum.COMPLETED.matches(serviceOrder.getOrderStatus())) {
            return true;
        }
        if (OrderStatusEnum.CANCELED.matches(serviceOrder.getOrderStatus())) {
            throw new BusinessException("已取消订单不能完成");
        }

        List<ServiceOrderItem> orderItems = serviceOrderItemService.list(
                new LambdaQueryWrapper<ServiceOrderItem>().eq(ServiceOrderItem::getOrderId, id));
        deductInventoryForServiceOrder(serviceOrder, orderItems);

        ServiceOrder updateOrder = new ServiceOrder();
        updateOrder.setId(id);
        updateOrder.setOrderStatus(OrderStatusEnum.COMPLETED.getCode());
        updateOrder.setUpdateTime(LocalDateTime.now());
        if (serviceOrderMapper.updateById(updateOrder) != 1) {
            throw new BusinessException("完成订单失败");
        }
        return true;
    }

    private void deductInventoryForServiceOrder(ServiceOrder serviceOrder, List<ServiceOrderItem> orderItems) {
        if (!OrderTypeEnum.SERVICE.matches(serviceOrder.getOrderType())) {
            return;
        }
        if (orderItems == null || orderItems.isEmpty()) {
            throw new BusinessException("订单没有项目明细，不能完成服务订单");
        }

        Set<Long> serviceProjectIds = orderItems.stream()
                .map(ServiceOrderItem::getServiceProjectId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (serviceProjectIds.isEmpty()) {
            throw new BusinessException("订单项目缺少服务项目，不能完成服务订单");
        }

        List<ServiceProjectInventory> relations = serviceProjectInventoryService.listActiveByProjectIds(serviceProjectIds);
        if (relations.isEmpty()) {
            throw new BusinessException("服务项目未配置耗材关系，不能完成订单");
        }

        Map<Long, List<ServiceProjectInventory>> relationsByProject = relations.stream()
                .collect(Collectors.groupingBy(ServiceProjectInventory::getServiceProjectId));
        Set<Long> missingProjectIds = new LinkedHashSet<>(serviceProjectIds);
        missingProjectIds.removeAll(relationsByProject.keySet());
        if (!missingProjectIds.isEmpty()) {
            throw new BusinessException("服务项目未配置耗材关系，不能完成订单");
        }
        Map<Long, BigDecimal> quantityByInventory = new LinkedHashMap<>();

        for (ServiceOrderItem orderItem : orderItems) {
            List<ServiceProjectInventory> projectRelations = relationsByProject.get(orderItem.getServiceProjectId());
            if (projectRelations == null || projectRelations.isEmpty()) {
                continue;
            }

            BigDecimal itemQuantity = positiveQuantity(orderItem.getQuantity());
            for (ServiceProjectInventory relation : projectRelations) {
                BigDecimal consumeQuantity = positiveQuantity(relation.getConsumeQuantity());
                BigDecimal stockOutQuantity = consumeQuantity.multiply(itemQuantity);
                quantityByInventory.merge(relation.getInventoryId(), stockOutQuantity, BigDecimal::add);
            }
        }
        if (quantityByInventory.isEmpty()) {
            throw new BusinessException("服务项目未配置有效耗材关系，不能完成订单");
        }

        for (Map.Entry<Long, BigDecimal> entry : quantityByInventory.entrySet()) {
            recordOrderStockOut(serviceOrder, entry.getKey(), entry.getValue());
        }
    }

    private Map<Long, String> loadStaffNames(Set<Long> staffIds) {
        if (staffIds.isEmpty()) {
            return Map.of();
        }
        Collection<StaffMember> staffMembers = staffMemberService.listByIds(staffIds);
        if (staffMembers == null || staffMembers.isEmpty()) {
            return Map.of();
        }
        return staffMembers.stream()
                .filter(staffMember -> staffMember.getId() != null)
                .collect(Collectors.toMap(
                        StaffMember::getId,
                        StaffMember::getName,
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    private void recordOrderStockOut(ServiceOrder serviceOrder, Long inventoryId, BigDecimal stockOutQuantity) {
        InventoryStockLogDTO dto = new InventoryStockLogDTO();
        dto.setInventoryId(inventoryId);
        dto.setChangeType(InventoryChangeTypeEnum.STOCK_OUT.getCode());
        dto.setChangeQuantity(stockOutQuantity);
        dto.setRelatedOrderId(serviceOrder.getId());
        dto.setOperatorId(BaseContext.getCurrentId());
        dto.setRemark("订单完成自动扣库存，订单号：" + serviceOrder.getOrderNo());
        inventoryStockLogService.recordStockChange(dto, InventoryChangeTypeEnum.STOCK_OUT.getCode());
    }

    private BigDecimal positiveQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("扣库存数量必须大于0");
        }
        return quantity;
    }
}
