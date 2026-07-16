package com.wkr.storeserver.service;

import com.wkr.storepojo.enums.RoleEnum;
import com.wkr.storepojo.vo.DashboardOverviewVO;
import com.wkr.storeserver.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplDataScopeTest {

    @Mock
    private CustomerProfileService customerProfileService;
    @Mock
    private AppointmentService appointmentService;
    @Mock
    private ServiceOrderService serviceOrderService;
    @Mock
    private InventorySkuService inventorySkuService;
    @Mock
    private PermissionPointService permissionPointService;

    private DashboardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DashboardServiceImpl(
                customerProfileService,
                appointmentService,
                serviceOrderService,
                inventorySkuService,
                permissionPointService);
    }

    @Test
    void staffDashboardUsesVisibleEmployeeCountsAndLists() {
        when(permissionPointService.listEffectiveCodes(9L, RoleEnum.STAFF))
                .thenReturn(List.of("customers:view", "appointments:view", "serviceOrders:view"));
        when(customerProfileService.countVisibleCustomers()).thenReturn(3L);
        when(appointmentService.countVisibleAppointments()).thenReturn(5L);
        when(serviceOrderService.countVisibleOrders()).thenReturn(4L);

        DashboardOverviewVO overview = service.getOverview(9L, RoleEnum.STAFF.getCode());

        assertEquals(3L, overview.getCustomerTotal());
        assertEquals(5L, overview.getAppointmentTotal());
        assertEquals(4L, overview.getOrderTotal());
        verify(customerProfileService, never()).count();
        verify(customerProfileService).countVisibleCustomers();
        verify(appointmentService).listRecent(5);
        verify(serviceOrderService).listPendingOrderSummaries(5);
    }
}
