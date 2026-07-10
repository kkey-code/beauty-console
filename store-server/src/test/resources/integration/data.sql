INSERT INTO staff_member (id, name, phone, gender, position, status, remark, create_time, update_time)
VALUES
    (1, 'Admin', '13800000001', 1, 'manager', 1, 'integration', NOW(), NOW()),
    (2, 'Inventory', '13800000002', 1, 'inventory', 1, 'integration', NOW(), NOW());

INSERT INTO sys_user (id, username, password_hash, role_id, staff_id, status, last_login_time, create_time, update_time)
VALUES
    (1, 'admin', '$2a$10$8CNVvlRfFo6bTbYtxDLW1edK3my3Esj0rXuV.pKqhLX4zUKpdqrRC', 1, 1, 1, NOW(), NOW(), NOW()),
    (2, 'inventory', '$2a$10$8CNVvlRfFo6bTbYtxDLW1edK3my3Esj0rXuV.pKqhLX4zUKpdqrRC', 4, 2, 1, NOW(), NOW(), NOW());

INSERT INTO inventory_sku (
    id, name, category, unit, quantity, safety_stock, cost_price, supplier, status, remark, create_time, update_time
) VALUES (
    1001, 'Bubble Cleaner', 'material', 'bottle', 5.00, 2.00, 68.00, 'integration supplier', 1, 'integration', NOW(), NOW()
);

INSERT INTO sys_permission (
    id, permission_code, permission_name, permission_group, method, path_pattern, menu_path, action_key, status, create_time, update_time
) VALUES
    (1001, 'dashboard:view', '工作台', '工作台', NULL, NULL, '/dashboard', 'dashboard:view', 1, NOW(), NOW()),
    (1016, 'inventorySkus:view', '库存耗材查看', '库存中心', 'GET', '/admin/inventory-skus/**', '/inventory-skus', 'inventorySkus:view', 1, NOW(), NOW()),
    (1026, 'inventoryStockLogs:create', '库存流水新增', '库存中心', 'POST', '/admin/inventory-stock-logs/**', NULL, 'inventoryStockLogs:create', 1, NOW(), NOW()),
    (1051, 'users:view', '账号查看', '账号权限', 'GET', '/admin/users/**', '/users', 'users:view', 1, NOW(), NOW()),
    (1057, 'permissions:view', '权限点查看', '账号权限', 'GET', '/admin/permissions/**', NULL, 'permissions:view', 1, NOW(), NOW());

INSERT INTO sys_role_permission (role_id, permission_id, create_time)
VALUES
    (1, 1001, NOW()),
    (1, 1016, NOW()),
    (1, 1026, NOW()),
    (1, 1051, NOW()),
    (1, 1057, NOW()),
    (4, 1001, NOW()),
    (4, 1016, NOW()),
    (4, 1026, NOW());
