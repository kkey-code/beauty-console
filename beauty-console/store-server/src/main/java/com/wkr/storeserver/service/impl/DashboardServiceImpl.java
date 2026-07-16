package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wkr.storepojo.entity.InventorySku;
import com.wkr.storepojo.enums.CommonStatusEnum;
import com.wkr.storepojo.enums.RoleEnum;
import com.wkr.storepojo.vo.DashboardOverviewVO;
import com.wkr.storepojo.vo.InventorySkuVO;
import com.wkr.storeserver.service.AppointmentService;
import com.wkr.storeserver.service.CustomerProfileService;
import com.wkr.storeserver.service.DashboardService;
import com.wkr.storeserver.service.InventorySkuService;
import com.wkr.storeserver.service.PermissionPointService;
import com.wkr.storeserver.service.ServiceOrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 将工作台原来的七次 HTTP 请求聚合为一次，并短暂缓存每个用户的结果。
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private static final int DASHBOARD_LIST_SIZE = 5;

    private final CustomerProfileService customerProfileService;
    private final AppointmentService appointmentService;
    private final ServiceOrderService serviceOrderService;
    private final InventorySkuService inventorySkuService;
    private final PermissionPointService permissionPointService;

    public DashboardServiceImpl(
            CustomerProfileService customerProfileService,
            AppointmentService appointmentService,
            ServiceOrderService serviceOrderService,
            InventorySkuService inventorySkuService,
            PermissionPointService permissionPointService) {
        this.customerProfileService = customerProfileService;
        this.appointmentService = appointmentService;
        this.serviceOrderService = serviceOrderService;
        this.inventorySkuService = inventorySkuService;
        this.permissionPointService = permissionPointService;
    }

    @Override
    @Cacheable(
            cacheNames = "dashboard:overview",
            key = "#p0 + ':' + #p1",
            sync = true)
    public DashboardOverviewVO getOverview(Long userId, Integer roleId) {
        return buildOverview(userId, roleId);
    }

    @Override
    @CachePut(cacheNames = "dashboard:overview", key = "#p0 + ':' + #p1")
    public DashboardOverviewVO refreshOverview(Long userId, Integer roleId) {
        return buildOverview(userId, roleId);
    }

    private DashboardOverviewVO buildOverview(Long userId, Integer roleId) {
        RoleEnum role = RoleEnum.fromCode(roleId);
        Set<String> permissions = role == null
                ? Set.of()
                : new HashSet<>(permissionPointService.listEffectiveCodes(userId, role));

        DashboardOverviewVO overview = new DashboardOverviewVO();
        if (canView(role, permissions, "customers:view")) {
            overview.setCustomerTotal(customerProfileService.countVisibleCustomers());
        }
        if (canView(role, permissions, "appointments:view")) {
            overview.setAppointmentTotal(appointmentService.countVisibleAppointments());
            overview.setAppointments(appointmentService.listRecent(DASHBOARD_LIST_SIZE));
        }
        if (canView(role, permissions, "serviceOrders:view")) {
            overview.setOrderTotal(serviceOrderService.countVisibleOrders());
            overview.setPendingOrders(serviceOrderService.listPendingOrderSummaries(DASHBOARD_LIST_SIZE));
        }
        if (canView(role, permissions, "inventorySkus:view")) {
            overview.setInventoryTotal(inventorySkuService.count());
            overview.setLowStockItems(listLowStockItems(DASHBOARD_LIST_SIZE));
        }
        return overview;
    }

    private boolean canView(RoleEnum role, Set<String> permissions, String permission) {
        return role == RoleEnum.SUPER_ADMIN || permissions.contains(permission);
    }

    private List<InventorySkuVO> listLowStockItems(int limit) {
        int normalizedLimit = Math.max(1, Math.min(limit, 50));
        List<InventorySku> rows = inventorySkuService.list(new LambdaQueryWrapper<InventorySku>()
                .apply("quantity <= safety_stock")
                .orderByAsc(InventorySku::getQuantity)
                .orderByAsc(InventorySku::getId)
                .last("LIMIT " + normalizedLimit));
        return rows.stream().map(this::toInventorySkuVO).toList();
    }

    private InventorySkuVO toInventorySkuVO(InventorySku inventorySku) {
        InventorySkuVO vo = new InventorySkuVO();
        BeanUtils.copyProperties(inventorySku, vo);
        vo.setStatusName(CommonStatusEnum.labelOf(inventorySku.getStatus()));
        BigDecimal quantity = inventorySku.getQuantity();
        BigDecimal safetyStock = inventorySku.getSafetyStock();
        vo.setBelowSafetyStock(quantity != null
                && safetyStock != null
                && quantity.compareTo(safetyStock) < 0);
        return vo;
    }
}
