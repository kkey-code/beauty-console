package com.wkr.storeserver.config;

import com.wkr.storeserver.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Locale;

/**
 * Existing Docker volumes are not recreated when an image is updated. This initializer
 * applies the small backwards-compatible schema additions required by the current code
 * before automatically provisioning missing employee accounts.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(
        name = "store.database.compatibility-initializer-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class DatabaseCompatibilityInitializer implements ApplicationRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final SysUserService sysUserService;

    public DatabaseCompatibilityInitializer(
            DataSource dataSource,
            JdbcTemplate jdbcTemplate,
            SysUserService sysUserService) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.sysUserService = sysUserService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String databaseProduct = connection.getMetaData().getDatabaseProductName();
            if (databaseProduct == null
                    || !databaseProduct.toLowerCase(Locale.ROOT).contains("mysql")) {
                return;
            }
        }

        ensureCustomerOwnerColumn();
        ensureOneAccountPerStaffIndex();
        ensurePermissionManagementPoints();
        sysUserService.ensureAccountsForAllStaff();
    }

    private void ensureCustomerOwnerColumn() {
        if (!columnExists("customer_profile", "owner_staff_id")) {
            log.info("Adding missing customer_profile.owner_staff_id column");
            jdbcTemplate.execute("ALTER TABLE customer_profile "
                    + "ADD COLUMN owner_staff_id BIGINT NULL COMMENT '建档员工ID，用于员工数据范围' AFTER source");
        }
        if (!indexExists("customer_profile", "idx_customer_owner_staff")) {
            log.info("Adding missing idx_customer_owner_staff index");
            jdbcTemplate.execute("ALTER TABLE customer_profile "
                    + "ADD INDEX idx_customer_owner_staff (owner_staff_id)");
        }
    }

    private void ensureOneAccountPerStaffIndex() {
        if (indexExists("sys_user", "uk_sys_user_staff")) {
            return;
        }
        Long duplicateGroups = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ("
                        + "SELECT staff_id FROM sys_user WHERE staff_id IS NOT NULL "
                        + "GROUP BY staff_id HAVING COUNT(*) > 1"
                        + ") duplicate_staff",
                Long.class);
        if (duplicateGroups != null && duplicateGroups > 0) {
            throw new IllegalStateException("sys_user 存在重复员工账号，请先合并重复账号后再启动");
        }
        log.info("Adding missing uk_sys_user_staff unique index");
        jdbcTemplate.execute("ALTER TABLE sys_user ADD UNIQUE INDEX uk_sys_user_staff (staff_id)");
    }

    private void ensurePermissionManagementPoints() {
        jdbcTemplate.update(
                "INSERT INTO sys_permission (permission_code, permission_name, permission_group, method, "
                        + "path_pattern, menu_path, action_key, status, create_time, update_time) "
                        + "VALUES ('roles:permissions', '角色默认权限配置', '账号权限', '*', "
                        + "'/admin/permissions/roles/**', NULL, 'roles:permissions', 1, NOW(), NOW()) "
                        + "ON DUPLICATE KEY UPDATE permission_name = VALUES(permission_name), "
                        + "method = VALUES(method), path_pattern = VALUES(path_pattern), status = 1");
        jdbcTemplate.update(
                "INSERT INTO sys_permission (permission_code, permission_name, permission_group, method, "
                        + "path_pattern, menu_path, action_key, status, create_time, update_time) "
                        + "VALUES ('users:resetPassword', '账号密码重置', '账号权限', 'PATCH', "
                        + "'/admin/users/*/reset-password', NULL, 'users:resetPassword', 1, NOW(), NOW()) "
                        + "ON DUPLICATE KEY UPDATE permission_name = VALUES(permission_name), "
                        + "method = VALUES(method), path_pattern = VALUES(path_pattern), status = 1");
        jdbcTemplate.update(
                "INSERT IGNORE INTO sys_role_permission (role_id, permission_id, create_time) "
                        + "SELECT role_id, permission_id, NOW() FROM ("
                        + "SELECT roles.role_id, permission.id AS permission_id "
                        + "FROM (SELECT 1 AS role_id UNION ALL SELECT 2) roles "
                        + "JOIN sys_permission permission ON permission.permission_code IN "
                        + "('roles:permissions', 'users:resetPassword')"
                        + ") role_permission");
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns "
                        + "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class,
                tableName,
                columnName);
        return count != null && count > 0;
    }

    private boolean indexExists(String tableName, String indexName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.statistics "
                        + "WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?",
                Integer.class,
                tableName,
                indexName);
        return count != null && count > 0;
    }
}
