package com.wkr.storeserver.security;

import com.wkr.storepojo.enums.RoleEnum;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 角色权限检查器，按照角色、HTTP 方法和接口路径判断当前请求是否允许执行。
 */
@Component
public class RolePermissionChecker {

    private static final List<String> STAFF_WRITE_PATHS = List.of(
            "/admin/customers",
            "/admin/appointments",
            "/admin/appointment-items",
            "/admin/service-orders",
            "/admin/service-order-items");

    private static final List<String> INVENTORY_PATHS = List.of(
            "/admin/inventory-skus",
            "/admin/inventory-stock-logs",
            "/admin/service-project-inventories");

    private static final String PAYMENT_PATH = "/admin/payment-records";

    private static final List<String> FINANCE_READ_PATHS = List.of(
            "/admin/customers",
            "/admin/service-orders",
            "/admin/service-order-items");

    public boolean isAllowed(RoleEnum role, String method, String path) {
        if (role == null || method == null || path == null) {
            return false;
        }
        String normalizedMethod = method.toUpperCase(Locale.ROOT);

        if (role == RoleEnum.SUPER_ADMIN || role == RoleEnum.STORE_MANAGER) {
            return true;
        }

        boolean readRequest = isReadRequest(normalizedMethod);
        if (role == RoleEnum.READONLY) {
            return readRequest;
        }
        if (role == RoleEnum.INVENTORY_ADMIN) {
            return isInventoryAllowed(normalizedMethod, path);
        }
        if (role == RoleEnum.FINANCE) {
            return isPaymentWrite(normalizedMethod, path)
                    || readRequest && matchesAny(path, FINANCE_READ_PATHS);
        }
        if (role == RoleEnum.STAFF) {
            return isStaffBusinessAllowed(normalizedMethod, path)
                    || readRequest && matchesPath(path, "/admin/service-projects");
        }
        return false;
    }

    private boolean isReadRequest(String method) {
        return "GET".equals(method) || "HEAD".equals(method);
    }

    private boolean isStaffBusinessAllowed(String method, String path) {
        if (!matchesAny(path, STAFF_WRITE_PATHS)) {
            return false;
        }
        return isReadRequest(method) || Set.of("POST", "PUT", "PATCH").contains(method);
    }

    private boolean isInventoryAllowed(String method, String path) {
        if (!matchesAny(path, INVENTORY_PATHS)) {
            return false;
        }
        return isReadRequest(method) || Set.of("POST", "PUT", "PATCH").contains(method);
    }

    private boolean isPaymentWrite(String method, String path) {
        if (!matchesPath(path, PAYMENT_PATH)) {
            return false;
        }
        return isReadRequest(method) || Set.of("POST", "PATCH").contains(method);
    }

    private boolean matchesAny(String path, List<String> prefixes) {
        return prefixes.stream().anyMatch(prefix -> matchesPath(path, prefix));
    }

    private boolean matchesPath(String path, String prefix) {
        return path.equals(prefix) || path.startsWith(prefix + "/");
    }
}
