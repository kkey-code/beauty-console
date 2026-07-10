package com.wkr.storeserver.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.InventoryStockLogDTO;
import com.wkr.storepojo.entity.InventoryStockLog;
import com.wkr.storepojo.entity.ServiceOrder;
import com.wkr.storepojo.entity.ServiceOrderItem;
import com.wkr.storepojo.entity.ServiceProjectInventory;
import com.wkr.storepojo.entity.StaffMember;
import com.wkr.storepojo.enums.InventoryChangeTypeEnum;
import com.wkr.storepojo.enums.OrderStatusEnum;
import com.wkr.storepojo.enums.OrderTypeEnum;
import com.wkr.storepojo.vo.ServiceOrderItemVO;
import com.wkr.storeserver.mapper.ServiceOrderMapper;
import com.wkr.storeserver.service.impl.ServiceOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 订单完成服务测试，验证完成服务订单时按服务项目耗材配置自动扣减库存。
 */
@ExtendWith(MockitoExtension.class)
class ServiceOrderServiceImplFinishTest {

    @Mock
    private ServiceOrderMapper serviceOrderMapper;

    @Mock
    private ServiceOrderItemService serviceOrderItemService;

    @Mock
    private PaymentRecordService paymentRecordService;

    @Mock
    private StaffMemberService staffMemberService;

    @Mock
    private ServiceProjectInventoryService serviceProjectInventoryService;

    @Mock
    private InventoryStockLogService inventoryStockLogService;

    private ServiceOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ServiceOrderServiceImpl(
                serviceOrderMapper,
                serviceOrderItemService,
                paymentRecordService,
                staffMemberService,
                serviceProjectInventoryService,
                inventoryStockLogService);
    }

    @Test
    void finishServiceOrderDeductsConfiguredInventory() {
        when(serviceOrderMapper.selectOne(any(Wrapper.class))).thenReturn(serviceOrder(OrderStatusEnum.PENDING));
        when(serviceOrderItemService.list(any(Wrapper.class))).thenReturn(List.of(orderItem(2)));
        when(serviceProjectInventoryService.listActiveByProjectIds(any()))
                .thenReturn(List.of(projectInventory(1001L, "1.50"), projectInventory(1002L, "2.00")));
        when(inventoryStockLogService.recordStockChange(any(InventoryStockLogDTO.class), eq("stock_out")))
                .thenReturn(new InventoryStockLog());
        when(serviceOrderMapper.updateById(any(ServiceOrder.class))).thenReturn(1);

        boolean result = service.finish(2001L);

        assertTrue(result);

        ArgumentCaptor<InventoryStockLogDTO> dtoCaptor = ArgumentCaptor.forClass(InventoryStockLogDTO.class);
        verify(inventoryStockLogService, times(2))
                .recordStockChange(dtoCaptor.capture(), eq(InventoryChangeTypeEnum.STOCK_OUT.getCode()));

        List<InventoryStockLogDTO> dtos = dtoCaptor.getAllValues();
        assertEquals(1001L, dtos.get(0).getInventoryId());
        assertEquals(0, new BigDecimal("3.00").compareTo(dtos.get(0).getChangeQuantity()));
        assertEquals(2001L, dtos.get(0).getRelatedOrderId());

        assertEquals(1002L, dtos.get(1).getInventoryId());
        assertEquals(0, new BigDecimal("4.00").compareTo(dtos.get(1).getChangeQuantity()));

        ArgumentCaptor<ServiceOrder> updateCaptor = ArgumentCaptor.forClass(ServiceOrder.class);
        verify(serviceOrderMapper).updateById(updateCaptor.capture());
        assertEquals(OrderStatusEnum.COMPLETED.getCode(), updateCaptor.getValue().getOrderStatus());
    }

    @Test
    void completedOrderDoesNotDeductAgain() {
        when(serviceOrderMapper.selectOne(any(Wrapper.class))).thenReturn(serviceOrder(OrderStatusEnum.COMPLETED));

        boolean result = service.finish(2001L);

        assertTrue(result);
        verifyNoInteractions(serviceOrderItemService);
        verifyNoInteractions(serviceProjectInventoryService);
        verifyNoInteractions(inventoryStockLogService);
        verify(serviceOrderMapper, never()).updateById(any(ServiceOrder.class));
    }

    @Test
    void finishServiceOrderRejectsMissingInventoryMapping() {
        when(serviceOrderMapper.selectOne(any(Wrapper.class))).thenReturn(serviceOrder(OrderStatusEnum.PENDING));
        when(serviceOrderItemService.list(any(Wrapper.class))).thenReturn(List.of(orderItem(2)));
        when(serviceProjectInventoryService.listActiveByProjectIds(any())).thenReturn(List.of());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.finish(2001L));

        assertTrue(exception.getMessage().contains("未配置耗材关系"));
        verifyNoInteractions(inventoryStockLogService);
        verify(serviceOrderMapper, never()).updateById(any(ServiceOrder.class));
    }

    @Test
    void getOrderItemsBatchLoadsStaffNames() {
        ServiceOrderItem firstItem = orderItem(1);
        firstItem.setStaffId(5001L);
        ServiceOrderItem secondItem = orderItem(1);
        secondItem.setId(3002L);
        secondItem.setStaffId(5002L);
        when(serviceOrderItemService.list(any(Wrapper.class))).thenReturn(List.of(firstItem, secondItem));
        when(staffMemberService.listByIds(anyCollection()))
                .thenReturn(List.of(staffMember(5001L, "Alice"), staffMember(5002L, "Bob")));

        List<ServiceOrderItemVO> result = service.getOrderItemsByOrderId(2001L);

        assertEquals("Alice", result.get(0).getStaffName());
        assertEquals("Bob", result.get(1).getStaffName());
        verify(staffMemberService).listByIds(anyCollection());
        verify(staffMemberService, never()).getById(anyLong());
    }

    private ServiceOrder serviceOrder(OrderStatusEnum status) {
        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setId(2001L);
        serviceOrder.setOrderNo("ORD2001");
        serviceOrder.setOrderType(OrderTypeEnum.SERVICE.getCode());
        serviceOrder.setOrderStatus(status.getCode());
        return serviceOrder;
    }

    private ServiceOrderItem orderItem(int quantity) {
        ServiceOrderItem item = new ServiceOrderItem();
        item.setId(3001L);
        item.setOrderId(2001L);
        item.setServiceProjectId(4001L);
        item.setQuantity(BigDecimal.valueOf(quantity));
        return item;
    }

    private ServiceProjectInventory projectInventory(Long inventoryId, String consumeQuantity) {
        ServiceProjectInventory relation = new ServiceProjectInventory();
        relation.setServiceProjectId(4001L);
        relation.setInventoryId(inventoryId);
        relation.setConsumeQuantity(new BigDecimal(consumeQuantity));
        relation.setStatus(1);
        return relation;
    }

    private StaffMember staffMember(Long id, String name) {
        StaffMember staffMember = new StaffMember();
        staffMember.setId(id);
        staffMember.setName(name);
        return staffMember;
    }
}
