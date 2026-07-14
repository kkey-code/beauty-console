-- Permission points and default role permissions.
-- Run after patch_role_permissions.sql.

USE db_platform;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS sys_permission (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Permission ID',
    permission_code  VARCHAR(100) NOT NULL COMMENT 'Permission code',
    permission_name  VARCHAR(100) NOT NULL COMMENT 'Permission display name',
    permission_group VARCHAR(50)  NOT NULL COMMENT 'Permission group',
    method           VARCHAR(10)  DEFAULT NULL COMMENT 'HTTP method, empty means menu/action only',
    path_pattern     VARCHAR(200) DEFAULT NULL COMMENT 'Ant style API path pattern',
    menu_path        VARCHAR(100) DEFAULT NULL COMMENT 'Frontend menu path',
    action_key       VARCHAR(100) DEFAULT NULL COMMENT 'Frontend action key',
    status           TINYINT      NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Permission points';

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id            BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    role_id       INT    NOT NULL COMMENT 'Role ID',
    permission_id BIGINT NOT NULL COMMENT 'Permission ID',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    KEY idx_role_permission_role (role_id),
    KEY idx_role_permission_permission (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role permission relation';

CREATE TABLE IF NOT EXISTS sys_user_permission (
    id            BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    user_id       BIGINT NOT NULL COMMENT 'User ID',
    permission_id BIGINT NOT NULL COMMENT 'Permission ID',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_permission (user_id, permission_id),
    KEY idx_user_permission_user (user_id),
    KEY idx_user_permission_permission (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User permission override relation';

INSERT INTO sys_permission (
    id, permission_code, permission_name, permission_group, method, path_pattern, menu_path, action_key, status
) VALUES
    (1001, 'dashboard:view', '工作台', '工作台', NULL, NULL, '/dashboard', 'dashboard:view', 1),
    (1002, 'customers:view', '客户查看', '客户档案', 'GET', '/admin/customers/**', '/customers', 'customers:view', 1),
    (1003, 'customers:create', '客户新增', '客户档案', 'POST', '/admin/customers', NULL, 'customers:create', 1),
    (1004, 'customers:edit', '客户编辑', '客户档案', 'PUT', '/admin/customers/**', NULL, 'customers:edit', 1),
    (1005, 'customers:delete', '客户删除', '客户档案', 'DELETE', '/admin/customers/**', NULL, 'customers:delete', 1),
    (1006, 'staffMembers:view', '员工查看', '员工管理', 'GET', '/admin/staff-members/**', '/staff-members', 'staffMembers:view', 1),
    (1007, 'staffMembers:create', '员工新增', '员工管理', 'POST', '/admin/staff-members', NULL, 'staffMembers:create', 1),
    (1008, 'staffMembers:edit', '员工编辑', '员工管理', 'PUT', '/admin/staff-members/**', NULL, 'staffMembers:edit', 1),
    (1009, 'staffMembers:status', '员工状态', '员工管理', 'PATCH', '/admin/staff-members/*/status', NULL, 'staffMembers:status', 1),
    (1010, 'staffMembers:delete', '员工删除', '员工管理', 'DELETE', '/admin/staff-members/**', NULL, 'staffMembers:delete', 1),
    (1011, 'serviceProjects:view', '服务项目查看', '服务项目', 'GET', '/admin/service-projects/**', '/service-projects', 'serviceProjects:view', 1),
    (1012, 'serviceProjects:create', '服务项目新增', '服务项目', 'POST', '/admin/service-projects', NULL, 'serviceProjects:create', 1),
    (1013, 'serviceProjects:edit', '服务项目编辑', '服务项目', 'PUT', '/admin/service-projects/**', NULL, 'serviceProjects:edit', 1),
    (1014, 'serviceProjects:status', '服务项目状态', '服务项目', 'PATCH', '/admin/service-projects/*/status', NULL, 'serviceProjects:status', 1),
    (1015, 'serviceProjects:delete', '服务项目删除', '服务项目', 'DELETE', '/admin/service-projects/**', NULL, 'serviceProjects:delete', 1),
    (1016, 'inventorySkus:view', '库存耗材查看', '库存中心', 'GET', '/admin/inventory-skus/**', '/inventory-skus', 'inventorySkus:view', 1),
    (1017, 'inventorySkus:create', '库存耗材新增', '库存中心', 'POST', '/admin/inventory-skus', NULL, 'inventorySkus:create', 1),
    (1018, 'inventorySkus:edit', '库存耗材编辑', '库存中心', 'PUT', '/admin/inventory-skus/**', NULL, 'inventorySkus:edit', 1),
    (1019, 'inventorySkus:status', '库存耗材状态', '库存中心', 'PATCH', '/admin/inventory-skus/*/status', NULL, 'inventorySkus:status', 1),
    (1020, 'inventorySkus:delete', '库存耗材删除', '库存中心', 'DELETE', '/admin/inventory-skus/**', NULL, 'inventorySkus:delete', 1),
    (1021, 'serviceProjectInventories:view', '项目耗材查看', '库存中心', 'GET', '/admin/service-project-inventories/**', '/service-project-inventories', 'serviceProjectInventories:view', 1),
    (1022, 'serviceProjectInventories:create', '项目耗材新增', '库存中心', 'POST', '/admin/service-project-inventories', NULL, 'serviceProjectInventories:create', 1),
    (1023, 'serviceProjectInventories:edit', '项目耗材编辑', '库存中心', 'PUT', '/admin/service-project-inventories/**', NULL, 'serviceProjectInventories:edit', 1),
    (1024, 'serviceProjectInventories:delete', '项目耗材删除', '库存中心', 'DELETE', '/admin/service-project-inventories/**', NULL, 'serviceProjectInventories:delete', 1),
    (1025, 'inventoryStockLogs:view', '库存流水查看', '库存中心', 'GET', '/admin/inventory-stock-logs/**', '/inventory-stock-logs', 'inventoryStockLogs:view', 1),
    (1026, 'inventoryStockLogs:create', '库存流水新增', '库存中心', 'POST', '/admin/inventory-stock-logs/**', NULL, 'inventoryStockLogs:create', 1),
    (1027, 'appointments:view', '预约查看', '预约管理', 'GET', '/admin/appointments/**', '/appointments', 'appointments:view', 1),
    (1028, 'appointments:create', '预约新增', '预约管理', 'POST', '/admin/appointments', NULL, 'appointments:create', 1),
    (1029, 'appointments:edit', '预约编辑', '预约管理', 'PUT', '/admin/appointments/**', NULL, 'appointments:edit', 1),
    (1030, 'appointments:delete', '预约删除', '预约管理', 'DELETE', '/admin/appointments/**', NULL, 'appointments:delete', 1),
    (1031, 'appointments:confirm', '预约确认', '预约管理', 'PATCH', '/admin/appointments/*/confirm', NULL, 'appointments:confirm', 1),
    (1032, 'appointments:cancel', '预约取消', '预约管理', 'PATCH', '/admin/appointments/*/cancel', NULL, 'appointments:cancel', 1),
    (1033, 'appointments:toOrder', '预约转订单', '预约管理', 'POST', '/admin/service-orders/from-appointment/*', NULL, 'appointments:toOrder', 1),
    (1034, 'appointmentItems:view', '预约项目查看', '预约管理', 'GET', '/admin/appointment-items/**', NULL, 'appointmentItems:view', 1),
    (1035, 'appointmentItems:create', '预约项目新增', '预约管理', 'POST', '/admin/appointment-items', NULL, 'appointmentItems:create', 1),
    (1036, 'appointmentItems:edit', '预约项目编辑', '预约管理', 'PUT', '/admin/appointment-items/**', NULL, 'appointmentItems:edit', 1),
    (1037, 'appointmentItems:delete', '预约项目删除', '预约管理', 'DELETE', '/admin/appointment-items/**', NULL, 'appointmentItems:delete', 1),
    (1038, 'serviceOrders:view', '订单查看', '订单管理', 'GET', '/admin/service-orders/**', '/service-orders', 'serviceOrders:view', 1),
    (1039, 'serviceOrders:create', '订单新增', '订单管理', 'POST', '/admin/service-orders', NULL, 'serviceOrders:create', 1),
    (1040, 'serviceOrders:edit', '订单编辑', '订单管理', 'PUT', '/admin/service-orders/**', NULL, 'serviceOrders:edit', 1),
    (1041, 'serviceOrders:finish', '订单完成', '订单管理', 'PATCH', '/admin/service-orders/*/finish', NULL, 'serviceOrders:finish', 1),
    (1042, 'serviceOrders:cancel', '订单取消', '订单管理', 'PATCH', '/admin/service-orders/*/cancel', NULL, 'serviceOrders:cancel', 1),
    (1043, 'serviceOrders:delete', '订单删除', '订单管理', 'DELETE', '/admin/service-orders/**', NULL, 'serviceOrders:delete', 1),
    (1044, 'serviceOrderItems:view', '订单项目查看', '订单管理', 'GET', '/admin/service-order-items/**', NULL, 'serviceOrderItems:view', 1),
    (1045, 'serviceOrderItems:create', '订单项目新增', '订单管理', 'POST', '/admin/service-order-items', NULL, 'serviceOrderItems:create', 1),
    (1046, 'serviceOrderItems:edit', '订单项目编辑', '订单管理', 'PUT', '/admin/service-order-items/**', NULL, 'serviceOrderItems:edit', 1),
    (1047, 'serviceOrderItems:delete', '订单项目删除', '订单管理', 'DELETE', '/admin/service-order-items/**', NULL, 'serviceOrderItems:delete', 1),
    (1048, 'paymentRecords:view', '收款查看', '收款管理', 'GET', '/admin/payment-records/**', '/payment-records', 'paymentRecords:view', 1),
    (1049, 'paymentRecords:create', '收款新增', '收款管理', 'POST', '/admin/payment-records', NULL, 'paymentRecords:create', 1),
    (1050, 'paymentRecords:void', '收款作废', '收款管理', 'PATCH', '/admin/payment-records/*/void', NULL, 'paymentRecords:void', 1),
    (1051, 'users:view', '账号查看', '账号权限', 'GET', '/admin/users/**', '/users', 'users:view', 1),
    (1052, 'users:create', '账号新增', '账号权限', 'POST', '/admin/users', NULL, 'users:create', 1),
    (1053, 'users:edit', '账号编辑', '账号权限', 'PUT', '/admin/users/*', NULL, 'users:edit', 1),
    (1054, 'users:status', '账号状态', '账号权限', 'PATCH', '/admin/users/*/status', NULL, 'users:status', 1),
    (1055, 'users:delete', '账号删除', '账号权限', 'DELETE', '/admin/users/**', NULL, 'users:delete', 1),
    (1056, 'users:permissions', '账号权限分配', '账号权限', '*', '/admin/users/*/permissions', NULL, 'users:permissions', 1),
    (1057, 'permissions:view', '权限点查看', '账号权限', 'GET', '/admin/permissions/**', NULL, 'permissions:view', 1)
ON DUPLICATE KEY UPDATE
    permission_name = VALUES(permission_name),
    permission_group = VALUES(permission_group),
    method = VALUES(method),
    path_pattern = VALUES(path_pattern),
    menu_path = VALUES(menu_path),
    action_key = VALUES(action_key),
    status = VALUES(status);

DELETE FROM sys_role_permission;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE status = 1
UNION ALL
SELECT 2, id FROM sys_permission WHERE status = 1
UNION ALL
SELECT 3, id FROM sys_permission
WHERE permission_code IN (
    'dashboard:view',
    'customers:view', 'customers:create', 'customers:edit',
    'serviceProjects:view',
    'appointments:view', 'appointments:create', 'appointments:edit', 'appointments:confirm', 'appointments:cancel', 'appointments:toOrder',
    'appointmentItems:view', 'appointmentItems:create', 'appointmentItems:edit', 'appointmentItems:delete',
    'serviceOrders:view', 'serviceOrders:create', 'serviceOrders:edit', 'serviceOrders:finish', 'serviceOrders:cancel',
    'serviceOrderItems:view', 'serviceOrderItems:create', 'serviceOrderItems:edit'
)
UNION ALL
SELECT 4, id FROM sys_permission
WHERE permission_code IN (
    'dashboard:view',
    'inventorySkus:view', 'inventorySkus:create', 'inventorySkus:edit', 'inventorySkus:status',
    'serviceProjectInventories:view', 'serviceProjectInventories:create', 'serviceProjectInventories:edit',
    'inventoryStockLogs:view', 'inventoryStockLogs:create'
)
UNION ALL
SELECT 5, id FROM sys_permission
WHERE permission_code IN (
    'dashboard:view',
    'customers:view',
    'serviceOrders:view',
    'serviceOrderItems:view',
    'paymentRecords:view', 'paymentRecords:create', 'paymentRecords:void'
)
UNION ALL
SELECT 6, id FROM sys_permission
WHERE permission_code IN (
    'dashboard:view',
    'customers:view',
    'serviceProjects:view',
    'inventorySkus:view',
    'serviceProjectInventories:view',
    'inventoryStockLogs:view',
    'appointments:view',
    'appointmentItems:view',
    'serviceOrders:view',
    'serviceOrderItems:view',
    'paymentRecords:view'
);
