package com.wkr.storeserver.security;

import com.wkr.storepojo.enums.RoleEnum;

import java.util.Set;

/**
 * Permission ceiling for each built-in role. Account/role configuration may remove
 * permissions, but cannot elevate a business role into an account administrator.
 */
public final class RolePermissionPolicy {

    private static final Set<String> STAFF_PERMISSIONS = Set.of(
            "dashboard:view",
            "customers:view", "customers:create", "customers:edit",
            "serviceProjects:view",
            "appointments:view", "appointments:create", "appointments:edit",
            "appointments:confirm", "appointments:cancel", "appointments:toOrder",
            "appointmentItems:view", "appointmentItems:create", "appointmentItems:edit", "appointmentItems:delete",
            "serviceOrders:view", "serviceOrders:create", "serviceOrders:edit",
            "serviceOrders:finish", "serviceOrders:cancel",
            "serviceOrderItems:view", "serviceOrderItems:create", "serviceOrderItems:edit");

    private static final Set<String> INVENTORY_PERMISSIONS = Set.of(
            "dashboard:view",
            "inventorySkus:view", "inventorySkus:create", "inventorySkus:edit", "inventorySkus:status",
            "serviceProjectInventories:view", "serviceProjectInventories:create", "serviceProjectInventories:edit",
            "inventoryStockLogs:view", "inventoryStockLogs:create");

    private static final Set<String> FINANCE_PERMISSIONS = Set.of(
            "dashboard:view", "customers:view", "serviceOrders:view", "serviceOrderItems:view",
            "paymentRecords:view", "paymentRecords:create", "paymentRecords:void");

    private static final Set<String> READONLY_PERMISSIONS = Set.of(
            "dashboard:view", "customers:view", "serviceProjects:view", "inventorySkus:view",
            "serviceProjectInventories:view", "inventoryStockLogs:view", "appointments:view",
            "appointmentItems:view", "serviceOrders:view", "serviceOrderItems:view", "paymentRecords:view");

    private RolePermissionPolicy() {
    }

    public static boolean isAllowed(RoleEnum role, String permissionCode) {
        if (role == null || permissionCode == null || permissionCode.isBlank()) {
            return false;
        }
        if (role == RoleEnum.SUPER_ADMIN || role == RoleEnum.STORE_MANAGER) {
            return true;
        }
        return switch (role) {
            case STAFF -> STAFF_PERMISSIONS.contains(permissionCode);
            case INVENTORY_ADMIN -> INVENTORY_PERMISSIONS.contains(permissionCode);
            case FINANCE -> FINANCE_PERMISSIONS.contains(permissionCode);
            case READONLY -> READONLY_PERMISSIONS.contains(permissionCode);
            default -> false;
        };
    }
}
