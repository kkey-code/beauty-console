package com.wkr.storeserver.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storepojo.dto.ServiceOrderPageQueryDTO;
import com.wkr.storepojo.entity.CustomerProfile;
import com.wkr.storepojo.entity.PaymentRecord;
import com.wkr.storepojo.entity.ServiceOrder;
import com.wkr.storepojo.entity.ServiceOrderItem;
import com.wkr.storepojo.entity.StaffMember;
import com.wkr.storepojo.enums.DebtStatusEnum;
import com.wkr.storepojo.enums.OrderStatusEnum;
import com.wkr.storepojo.enums.OrderTypeEnum;
import com.wkr.storepojo.enums.PayStatusEnum;
import com.wkr.storepojo.vo.ServiceOrderVO;
import com.wkr.storeserver.mapper.ServiceOrderMapper;
import com.wkr.storeserver.service.impl.ServiceOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 订单分页批量装配测试，防止客户、明细、员工和支付信息退化为逐订单查询。
 */
@ExtendWith(MockitoExtension.class)
class ServiceOrderServiceImplPageTest {

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
    @Mock
    private CustomerProfileService customerProfileService;
    @Mock
    private AppointmentService appointmentService;
    @Mock
    private AppointmentItemService appointmentItemService;
    @Mock
    private DeletionGuardService deletionGuardService;

    private ServiceOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ServiceOrderServiceImpl(
                serviceOrderMapper,
                serviceOrderItemService,
                paymentRecordService,
                staffMemberService,
                serviceProjectInventoryService,
                inventoryStockLogService,
                customerProfileService,
                appointmentService,
                appointmentItemService,
                deletionGuardService);
        ReflectionTestUtils.setField(service, "baseMapper", serviceOrderMapper);
    }

    @Test
    void pageOrdersLoadsAllRelatedDataInFixedBatchQueries() {
        ServiceOrder firstOrder = order(2001L, 3001L);
        ServiceOrder secondOrder = order(2002L, 3002L);
        when(serviceOrderMapper.selectPage(any(Page.class), any(Wrapper.class)))
                .thenAnswer(invocation -> {
                    Page<ServiceOrder> page = invocation.getArgument(0);
                    page.setRecords(List.of(firstOrder, secondOrder));
                    page.setTotal(2);
                    return page;
                });
        when(customerProfileService.listByIds(anyCollection()))
                .thenReturn(List.of(customer(3001L, "客户甲"), customer(3002L, "客户乙")));
        when(serviceOrderItemService.list(any(Wrapper.class)))
                .thenReturn(List.of(orderItem(4001L, 2001L, 5001L), orderItem(4002L, 2002L, 5002L)));
        when(staffMemberService.listByIds(anyCollection()))
                .thenReturn(List.of(staff(5001L, "员工甲"), staff(5002L, "员工乙")));
        when(paymentRecordService.list(any(Wrapper.class)))
                .thenReturn(List.of(payment(6001L, 2001L)));

        PageResult<ServiceOrderVO> result = service.pageOrders(new ServiceOrderPageQueryDTO());
        List<ServiceOrderVO> records = result.getRecords();

        assertEquals(2L, result.getTotal());
        assertEquals(2, records.size());
        assertEquals("客户甲", records.get(0).getCustomerName());
        assertEquals("员工甲", records.get(0).getItems().get(0).getStaffName());
        assertEquals(1, records.get(0).getPayments().size());
        assertTrue(records.get(1).getPayments().isEmpty());

        verify(customerProfileService, times(1)).listByIds(anyCollection());
        verify(customerProfileService, never()).getById(any());
        verify(serviceOrderItemService, times(1)).list(any(Wrapper.class));
        verify(staffMemberService, times(1)).listByIds(anyCollection());
        verify(paymentRecordService, times(1)).list(any(Wrapper.class));
    }

    @Test
    void emptyPageSkipsAllRelatedDataQueries() {
        when(serviceOrderMapper.selectPage(any(Page.class), any(Wrapper.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PageResult<ServiceOrderVO> result = service.pageOrders(new ServiceOrderPageQueryDTO());

        assertEquals(0L, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
        verifyNoInteractions(customerProfileService, serviceOrderItemService, staffMemberService, paymentRecordService);
    }

    private ServiceOrder order(Long id, Long customerId) {
        ServiceOrder order = new ServiceOrder();
        order.setId(id);
        order.setCustomerId(customerId);
        order.setOrderType(OrderTypeEnum.SERVICE.getCode());
        order.setOrderStatus(OrderStatusEnum.PENDING.getCode());
        order.setDebtStatus(DebtStatusEnum.INSTALLMENT.getCode());
        order.setPayStatus(PayStatusEnum.UNPAID.getCode());
        return order;
    }

    private CustomerProfile customer(Long id, String name) {
        CustomerProfile customer = new CustomerProfile();
        customer.setId(id);
        customer.setName(name);
        customer.setPhone("13800000000");
        return customer;
    }

    private ServiceOrderItem orderItem(Long id, Long orderId, Long staffId) {
        ServiceOrderItem item = new ServiceOrderItem();
        item.setId(id);
        item.setOrderId(orderId);
        item.setStaffId(staffId);
        return item;
    }

    private StaffMember staff(Long id, String name) {
        StaffMember staffMember = new StaffMember();
        staffMember.setId(id);
        staffMember.setName(name);
        return staffMember;
    }

    private PaymentRecord payment(Long id, Long orderId) {
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setId(id);
        paymentRecord.setOrderId(orderId);
        return paymentRecord;
    }
}
