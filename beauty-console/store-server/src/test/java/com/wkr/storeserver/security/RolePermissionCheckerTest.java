package com.wkr.storeserver.security;

import com.wkr.storepojo.enums.RoleEnum;
import com.wkr.storeserver.service.PermissionPointService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 角色权限矩阵测试，验证六类角色在不同接口和请求方法上的允许与拒绝规则。
 */
class RolePermissionCheckerTest {

    private final RolePermissionChecker checker = new RolePermissionChecker();

    @Test
    void superAdminAndStoreManagerCanAccessAllAdminApis() {
        assertTrue(checker.isAllowed(RoleEnum.SUPER_ADMIN, "DELETE", "/admin/users/1"));
        assertTrue(checker.isAllowed(RoleEnum.STORE_MANAGER, "POST", "/admin/inventory-stock-logs/outbound"));
    }

    @Test
    void readonlyCanOnlyRead() {
        assertTrue(checker.isAllowed(RoleEnum.READONLY, "GET", "/admin/payment-records"));
        assertTrue(checker.isAllowed(RoleEnum.READONLY, "HEAD", "/admin/users/1"));
        assertFalse(checker.isAllowed(RoleEnum.READONLY, "POST", "/admin/customers"));
    }

    @Test
    void staffCanOperateDailyBusinessButNotInventoryOrPayments() {
        assertTrue(checker.isAllowed(RoleEnum.STAFF, "POST", "/admin/appointments"));
        assertTrue(checker.isAllowed(RoleEnum.STAFF, "PATCH", "/admin/service-orders/1/finish"));
        assertTrue(checker.isAllowed(RoleEnum.STAFF, "GET", "/admin/service-projects"));
        assertTrue(checker.isAllowed(RoleEnum.STAFF, "GET", "/admin/dashboard/overview"));
        assertFalse(checker.isAllowed(RoleEnum.STAFF, "POST", "/admin/service-projects"));
        assertFalse(checker.isAllowed(RoleEnum.STAFF, "DELETE", "/admin/customers/1"));
        assertFalse(checker.isAllowed(RoleEnum.STAFF, "DELETE", "/admin/service-orders/1"));
        assertFalse(checker.isAllowed(RoleEnum.STAFF, "GET", "/admin/payment-records"));
        assertFalse(checker.isAllowed(RoleEnum.STAFF, "POST", "/admin/inventory-stock-logs/outbound"));
    }

    @Test
    void inventoryAdminCanOnlyAccessInventoryApis() {
        assertTrue(checker.isAllowed(RoleEnum.INVENTORY_ADMIN, "GET", "/admin/inventory-skus"));
        assertTrue(checker.isAllowed(RoleEnum.INVENTORY_ADMIN, "POST", "/admin/inventory-stock-logs/inbound"));
        assertTrue(checker.isAllowed(RoleEnum.INVENTORY_ADMIN, "POST", "/admin/service-project-inventories"));
        assertFalse(checker.isAllowed(RoleEnum.INVENTORY_ADMIN, "DELETE", "/admin/inventory-skus/1"));
        assertFalse(checker.isAllowed(RoleEnum.INVENTORY_ADMIN, "DELETE", "/admin/service-project-inventories/1"));
        assertFalse(checker.isAllowed(RoleEnum.INVENTORY_ADMIN, "GET", "/admin/customers"));
    }

    @Test
    void financeCanOperatePaymentsAndReadRelatedBusinessData() {
        assertTrue(checker.isAllowed(RoleEnum.FINANCE, "POST", "/admin/payment-records"));
        assertTrue(checker.isAllowed(RoleEnum.FINANCE, "PATCH", "/admin/payment-records/1/void"));
        assertTrue(checker.isAllowed(RoleEnum.FINANCE, "GET", "/admin/service-orders/1"));
        assertTrue(checker.isAllowed(RoleEnum.FINANCE, "GET", "/admin/customers/1"));
        assertFalse(checker.isAllowed(RoleEnum.FINANCE, "PUT", "/admin/service-orders/1"));
        assertFalse(checker.isAllowed(RoleEnum.FINANCE, "GET", "/admin/inventory-skus"));
    }

    @Test
    void nonAdministrativeRolesAreDeniedUnknownPaths() {
        assertFalse(checker.isAllowed(RoleEnum.STAFF, "GET", "/admin/unknown"));
        assertFalse(checker.isAllowed(null, "GET", "/admin/customers"));
    }

    @Test
    void databasePermissionRulesOverrideLegacyRoleMatrix() {
        PermissionPointService permissionPointService = mock(PermissionPointService.class);
        when(permissionPointService.isPermissionModelReady()).thenReturn(true);
        when(permissionPointService.listEffectiveRules(1001L, RoleEnum.STAFF)).thenReturn(List.of(
                new PermissionRule("GET", "/admin/customers/**")
        ));

        RolePermissionChecker dbChecker = new RolePermissionChecker(permissionPointService);

        assertTrue(dbChecker.isAllowed(RoleEnum.STAFF, 1001L, "GET", "/admin/customers/1"));
        assertFalse(dbChecker.isAllowed(RoleEnum.STAFF, 1001L, "POST", "/admin/customers"));
    }
}
