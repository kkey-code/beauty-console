package com.wkr.storeserver.security;

import com.wkr.storepojo.enums.RoleEnum;
import com.wkr.storeserver.service.PermissionPointService;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Locale;
import java.util.Set;

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

    private final PermissionPointService permissionPointService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public RolePermissionChecker() {
        this.permissionPointService = null;
    }

    public RolePermissionChecker(PermissionPointService permissionPointService) {
        this.permissionPointService = permissionPointService;
    }

    public boolean isAllowed(RoleEnum role, String method, String path) {
        return isAllowed(role, null, method, path);
    }

    public boolean isAllowed(RoleEnum role, Long userId, String method, String path) {
        if (role == null || method == null || path == null) {
            return false;
        }

        String normalizedMethod = method.toUpperCase(Locale.ROOT);
        if (role == RoleEnum.SUPER_ADMIN) {
            return true;
        }

        if (permissionPointService != null) {
            try {
                if (permissionPointService.isPermissionModelReady()) {
                    return permissionPointService.listEffectiveRules(userId, role).stream()
                            .anyMatch(rule -> methodMatches(rule.method(), normalizedMethod)
                                    && pathMatches(rule.pathPattern(), path));
                }
            } catch (RuntimeException ignored) {
                // Keep the legacy matrix as a fallback before the SQL patch has been applied.
            }
        }

        return isAllowedByLegacyMatrix(role, normalizedMethod, path);
    }

    private boolean isAllowedByLegacyMatrix(RoleEnum role, String method, String path) {
        if (role == RoleEnum.STORE_MANAGER) {
            return true;
        }

        boolean readRequest = isReadRequest(method);
        if (role == RoleEnum.READONLY) {
            return readRequest;
        }
        if (role == RoleEnum.INVENTORY_ADMIN) {
            return isInventoryAllowed(method, path);
        }
        if (role == RoleEnum.FINANCE) {
            return isPaymentWrite(method, path)
                    || readRequest && matchesAny(path, FINANCE_READ_PATHS);
        }
        if (role == RoleEnum.STAFF) {
            return isStaffBusinessAllowed(method, path)
                    || readRequest && (matchesPath(path, "/admin/service-projects")
                    || matchesPath(path, "/admin/dashboard"));
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

    private boolean methodMatches(String ruleMethod, String requestMethod) {
        if (ruleMethod == null) {
            return false;
        }
        String normalizedRuleMethod = ruleMethod.toUpperCase(Locale.ROOT);
        return "*".equals(normalizedRuleMethod)
                || normalizedRuleMethod.equals(requestMethod)
                || ("GET".equals(normalizedRuleMethod) && "HEAD".equals(requestMethod));
    }

    private boolean pathMatches(String pattern, String path) {
        if (pattern == null) {
            return false;
        }
        if (pathMatcher.match(pattern, path)) {
            return true;
        }
        if (pattern.endsWith("/**")) {
            String basePath = pattern.substring(0, pattern.length() - 3);
            return path.equals(basePath) || path.startsWith(basePath + "/");
        }
        return false;
    }

    private boolean matchesAny(String path, List<String> prefixes) {
        return prefixes.stream().anyMatch(prefix -> matchesPath(path, prefix));
    }

    private boolean matchesPath(String path, String prefix) {
        return path.equals(prefix) || path.startsWith(prefix + "/");
    }
}
