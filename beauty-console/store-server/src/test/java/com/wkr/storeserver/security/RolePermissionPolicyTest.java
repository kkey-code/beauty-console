package com.wkr.storeserver.security;

import com.wkr.storepojo.enums.RoleEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RolePermissionPolicyTest {

    @Test
    void staffCanBeGrantedCustomerEditButNeverAccountManagement() {
        assertTrue(RolePermissionPolicy.isAllowed(RoleEnum.STAFF, "customers:edit"));
        assertFalse(RolePermissionPolicy.isAllowed(RoleEnum.STAFF, "users:view"));
        assertFalse(RolePermissionPolicy.isAllowed(RoleEnum.STAFF, "users:permissions"));
    }

    @Test
    void inventoryAndFinancePermissionsStayInsideTheirBusinessArea() {
        assertTrue(RolePermissionPolicy.isAllowed(RoleEnum.INVENTORY_ADMIN, "inventorySkus:edit"));
        assertFalse(RolePermissionPolicy.isAllowed(RoleEnum.INVENTORY_ADMIN, "customers:view"));
        assertTrue(RolePermissionPolicy.isAllowed(RoleEnum.FINANCE, "paymentRecords:create"));
        assertFalse(RolePermissionPolicy.isAllowed(RoleEnum.FINANCE, "serviceOrders:edit"));
    }

    @Test
    void readonlyRoleOnlyReceivesViewPermissions() {
        assertTrue(RolePermissionPolicy.isAllowed(RoleEnum.READONLY, "customers:view"));
        assertFalse(RolePermissionPolicy.isAllowed(RoleEnum.READONLY, "customers:edit"));
    }
}
