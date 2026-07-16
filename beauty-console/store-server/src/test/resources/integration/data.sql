INSERT INTO staff_member (id, name, phone, gender, position, status, remark, create_time, update_time)
VALUES
    (1, 'Admin', '13800000001', 1, 'manager', 1, 'integration', NOW(), NOW()),
    (2, 'Inventory', '13800000002', 1, 'inventory', 1, 'integration', NOW(), NOW()),
    (3, 'Staff Self Scope', '13800000003', 1, 'staff', 1, 'integration', NOW(), NOW()),
    (4, 'Unsafe ID Binding', '13800000004', 1, 'staff', 1, 'integration', NOW(), NOW());

INSERT INTO sys_user (id, username, password_hash, role_id, staff_id, status, last_login_time, create_time, update_time)
VALUES
    (1, 'admin', '$2a$10$8CNVvlRfFo6bTbYtxDLW1edK3my3Esj0rXuV.pKqhLX4zUKpdqrRC', 1, 1, 1, NOW(), NOW(), NOW()),
    (2, 'inventory', '$2a$10$8CNVvlRfFo6bTbYtxDLW1edK3my3Esj0rXuV.pKqhLX4zUKpdqrRC', 4, 2, 1, NOW(), NOW(), NOW()),
    (3, 'staff_scope', '$2a$10$8CNVvlRfFo6bTbYtxDLW1edK3my3Esj0rXuV.pKqhLX4zUKpdqrRC', 3, 3, 1, NOW(), NOW(), NOW());

INSERT INTO customer_profile (
    id, name, phone, gender, birthday, level, source, owner_staff_id, remark, create_time, update_time, deleted
) VALUES
    (1, 'Admin Customer', '13900000001', 1, NULL, 0, 'integration', 1, 'admin customer', NOW(), NOW(), 0),
    (2, 'Staff Customer', '13900000002', 2, NULL, 0, 'integration', 3, 'staff customer', NOW(), NOW(), 0);

INSERT INTO inventory_sku (
    id, name, category, unit, quantity, safety_stock, cost_price, supplier, status, remark, create_time, update_time
) VALUES (
    1001, 'Bubble Cleaner', 'material', 'bottle', 5.00, 2.00, 68.00, 'integration supplier', 1, 'integration', NOW(), NOW()
);

INSERT INTO service_project (
    id, name, category, price, duration_minutes, description, status, create_time, update_time
) VALUES (
    1001, 'Basic Facial', 'face', 168.00, 60, 'integration project', 1, NOW(), NOW()
);

INSERT INTO appointment (
    id, appointment_no, customer_id, staff_id, appointment_time, status, total_duration_minutes, remark, create_time, update_time, deleted
) VALUES
    (4001, 'APT-IT-1', 1, 1, NOW(), 0, 60, 'transition test', NOW(), NOW(), 0),
    (4002, 'APT-IT-2', 1, 1, NOW(), 1, 60, 'delete guard test', NOW(), NOW(), 0),
    (4003, 'APT-IT-STAFF', 2, 3, NOW(), 0, 60, 'staff scope test', NOW(), NOW(), 0);

INSERT INTO service_order (
    id, order_no, appointment_id, customer_id, order_type, original_amount, discount_amount, receivable_amount,
    paid_amount, debt_amount, debt_status, pay_status, order_status, remark, create_time, update_time, deleted
) VALUES
    (3001, 'ORD-IT-PAY-1', NULL, 1, 'service', 100.00, 0.00, 100.00, 0.00, 100.00, 1, 0, 0, 'payment test', NOW(), NOW(), 0),
    (3002, 'ORD-IT-APPOINTMENT', 4002, 1, 'service', 100.00, 0.00, 100.00, 0.00, 100.00, 1, 0, 0, 'appointment guard test', NOW(), NOW(), 0),
    (3003, 'ORD-IT-STAFF', 4003, 2, 'service', 168.00, 0.00, 168.00, 0.00, 168.00, 1, 0, 0, 'staff scope test', NOW(), NOW(), 0);

INSERT INTO service_order_item (
    id, order_id, service_project_id, service_name, unit_price, quantity,
    discount_amount, actual_amount, staff_id, remark, create_time
) VALUES
    (5001, 3003, 1001, 'Basic Facial', 168.00, 1.00, 0.00, 168.00, 3, 'staff scope test', NOW());

INSERT INTO sys_permission (
    id, permission_code, permission_name, permission_group, method, path_pattern, menu_path, action_key, status, create_time, update_time
) VALUES
    (1001, 'dashboard:view', '工作台', '工作台', 'GET', '/admin/dashboard/**', '/dashboard', 'dashboard:view', 1, NOW(), NOW()),
    (1002, 'customers:view', '客户查看', '客户档案', 'GET', '/admin/customers/**', '/customers', 'customers:view', 1, NOW(), NOW()),
    (1016, 'inventorySkus:view', '库存耗材查看', '库存中心', 'GET', '/admin/inventory-skus/**', '/inventory-skus', 'inventorySkus:view', 1, NOW(), NOW()),
    (1026, 'inventoryStockLogs:create', '库存流水新增', '库存中心', 'POST', '/admin/inventory-stock-logs/**', NULL, 'inventoryStockLogs:create', 1, NOW(), NOW()),
    (1021, 'serviceProjectInventories:view', '项目耗材查看', '库存中心', 'GET', '/admin/service-project-inventories/**', '/service-project-inventories', 'serviceProjectInventories:view', 1, NOW(), NOW()),
    (1022, 'serviceProjectInventories:create', '项目耗材新增', '库存中心', 'POST', '/admin/service-project-inventories', NULL, 'serviceProjectInventories:create', 1, NOW(), NOW()),
    (1023, 'serviceProjectInventories:edit', '项目耗材编辑', '库存中心', 'PUT', '/admin/service-project-inventories/**', NULL, 'serviceProjectInventories:edit', 1, NOW(), NOW()),
    (1024, 'serviceProjectInventories:delete', '项目耗材删除', '库存中心', 'DELETE', '/admin/service-project-inventories/**', NULL, 'serviceProjectInventories:delete', 1, NOW(), NOW()),
    (1027, 'appointments:view', '预约查看', '预约管理', 'GET', '/admin/appointments/**', '/appointments', 'appointments:view', 1, NOW(), NOW()),
    (1038, 'serviceOrders:view', '订单查看', '订单管理', 'GET', '/admin/service-orders/**', '/service-orders', 'serviceOrders:view', 1, NOW(), NOW()),
    (1051, 'users:view', '账号查看', '账号权限', 'GET', '/admin/users/**', '/users', 'users:view', 1, NOW(), NOW()),
    (1057, 'permissions:view', '权限点查看', '账号权限', 'GET', '/admin/permissions/**', NULL, 'permissions:view', 1, NOW(), NOW()),
    (1058, 'roles:permissions', '角色默认权限配置', '账号权限', '*', '/admin/permissions/roles/**', NULL, 'roles:permissions', 1, NOW(), NOW()),
    (1059, 'users:resetPassword', '账号密码重置', '账号权限', 'PATCH', '/admin/users/*/reset-password', NULL, 'users:resetPassword', 1, NOW(), NOW());

INSERT INTO sys_role_permission (role_id, permission_id, create_time)
VALUES
    (1, 1001, NOW()),
    (1, 1016, NOW()),
    (1, 1026, NOW()),
    (1, 1021, NOW()),
    (1, 1022, NOW()),
    (1, 1023, NOW()),
    (1, 1024, NOW()),
    (1, 1051, NOW()),
    (1, 1057, NOW()),
    (1, 1058, NOW()),
    (1, 1059, NOW()),
    (4, 1001, NOW()),
    (4, 1016, NOW()),
    (4, 1026, NOW()),
    (3, 1001, NOW()),
    (3, 1002, NOW()),
    (3, 1027, NOW()),
    (3, 1038, NOW());
