-- Add one enabled local test account for each built-in role.
-- Run patch_role_permissions.sql first if the database was created from the old 3-role schema.
-- Password for all accounts is 123456.

INSERT INTO staff_member (id, name, phone, gender, position, status, remark, create_time, update_time)
VALUES
    (1001, '测试超级管理员', '13800001001', 1, 'admin', 1, 'role test account', NOW(), NOW()),
    (1002, '测试店长', '13800001002', 2, 'manager', 1, 'role test account', NOW(), NOW()),
    (1003, '测试普通员工', '13800001003', 1, 'staff', 1, 'role test account', NOW(), NOW()),
    (1004, '测试库存管理员', '13800001004', 2, 'inventory', 1, 'role test account', NOW(), NOW()),
    (1005, '测试收银员', '13800001005', 1, 'cashier', 1, 'role test account', NOW(), NOW()),
    (1006, '测试只读账号', '13800001006', 2, 'readonly', 1, 'role test account', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    update_time = NOW();

INSERT INTO sys_user (id, username, password_hash, role_id, staff_id, status, last_login_time, create_time, update_time)
VALUES
    (1001, 'admin', '$2a$10$8CNVvlRfFo6bTbYtxDLW1edK3my3Esj0rXuV.pKqhLX4zUKpdqrRC', 1, 1001, 1, NOW(), NOW(), NOW()),
    (1002, 'manager', '$2a$10$8CNVvlRfFo6bTbYtxDLW1edK3my3Esj0rXuV.pKqhLX4zUKpdqrRC', 2, 1002, 1, NOW(), NOW(), NOW()),
    (1003, 'staff', '$2a$10$8CNVvlRfFo6bTbYtxDLW1edK3my3Esj0rXuV.pKqhLX4zUKpdqrRC', 3, 1003, 1, NOW(), NOW(), NOW()),
    (1004, 'inventory', '$2a$10$8CNVvlRfFo6bTbYtxDLW1edK3my3Esj0rXuV.pKqhLX4zUKpdqrRC', 4, 1004, 1, NOW(), NOW(), NOW()),
    (1005, 'finance', '$2a$10$8CNVvlRfFo6bTbYtxDLW1edK3my3Esj0rXuV.pKqhLX4zUKpdqrRC', 5, 1005, 1, NOW(), NOW(), NOW()),
    (1006, 'readonly', '$2a$10$8CNVvlRfFo6bTbYtxDLW1edK3my3Esj0rXuV.pKqhLX4zUKpdqrRC', 6, 1006, 1, NOW(), NOW(), NOW())
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    role_id = VALUES(role_id),
    staff_id = VALUES(staff_id),
    status = VALUES(status),
    update_time = NOW();
