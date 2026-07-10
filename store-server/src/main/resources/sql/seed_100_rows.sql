-- db_platform seed data for local API testing.
-- Password for admin/staff/readonly is 123456.

USE db_platform;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TEMPORARY TABLE IF EXISTS seed_seq;
CREATE TEMPORARY TABLE seed_seq (
    n INT PRIMARY KEY
);

INSERT INTO seed_seq (n)
SELECT ones.n + tens.n * 10 + 1 AS n
FROM (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
) ones
CROSS JOIN (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
) tens
ORDER BY n;

DELETE FROM inventory_stock_log WHERE id BETWEEN 1001 AND 1100;
DELETE FROM payment_record WHERE id BETWEEN 1001 AND 1100;
DELETE FROM service_order_item WHERE id BETWEEN 1001 AND 1100;
DELETE FROM service_order WHERE id BETWEEN 1001 AND 1100;
DELETE FROM appointment_item WHERE id BETWEEN 1001 AND 1100;
DELETE FROM appointment WHERE id BETWEEN 1001 AND 1100;
DELETE FROM sys_user WHERE id BETWEEN 1001 AND 1100;
DELETE FROM staff_member WHERE id BETWEEN 1001 AND 1100;
DELETE FROM customer_profile WHERE id BETWEEN 1001 AND 1100;
DELETE FROM service_project_inventory WHERE id BETWEEN 1001 AND 1100;
DELETE FROM service_project WHERE id BETWEEN 1001 AND 1100;
DELETE FROM inventory_sku WHERE id BETWEEN 1001 AND 1100;

INSERT INTO staff_member (
    id, name, phone, gender, position, status, remark, create_time, update_time
)
SELECT
    1000 + n,
    CONCAT('Staff ', LPAD(n, 3, '0')),
    CONCAT('138', LPAD(n, 8, '0')),
    CASE WHEN n % 2 = 0 THEN 1 ELSE 2 END,
    ELT(n % 8 + 1, 'manager', 'beautician', 'therapist', 'consultant', 'front_desk', 'cashier', 'trainee', 'operator'),
    CASE WHEN n % 10 = 0 THEN 0 ELSE 1 END,
    'seed staff',
    DATE_SUB(NOW(), INTERVAL n DAY),
    NOW()
FROM seed_seq;

INSERT INTO sys_user (
    id, username, password_hash, role_id, staff_id, status, last_login_time, create_time, update_time
)
VALUES
    (1001, 'admin', '$2a$10$8CNVvlRfFo6bTbYtxDLW1edK3my3Esj0rXuV.pKqhLX4zUKpdqrRC', 1, 1001, 1, NOW(), NOW(), NOW()),
    (1002, 'staff', '$2a$10$8CNVvlRfFo6bTbYtxDLW1edK3my3Esj0rXuV.pKqhLX4zUKpdqrRC', 3, 1002, 1, NOW(), NOW(), NOW()),
    (1003, 'readonly', '$2a$10$8CNVvlRfFo6bTbYtxDLW1edK3my3Esj0rXuV.pKqhLX4zUKpdqrRC', 6, 1003, 1, NOW(), NOW(), NOW());

INSERT INTO customer_profile (
    id, name, phone, gender, birthday, level, source, remark, create_time, update_time, deleted
)
SELECT
    1000 + n,
    CONCAT('Customer ', LPAD(n, 3, '0')),
    CONCAT('139', LPAD(n, 8, '0')),
    CASE WHEN n % 3 = 0 THEN 1 ELSE 2 END,
    DATE_SUB(CURDATE(), INTERVAL (22 + n % 38) YEAR),
    n % 4,
    ELT(n % 8 + 1, 'walk_in', 'referral', 'xiaohongshu', 'meituan', 'douyin', 'store_event', 'old_customer', 'online'),
    'seed customer',
    DATE_SUB(NOW(), INTERVAL n DAY),
    NOW(),
    CASE WHEN n % 25 = 0 THEN 1 ELSE 0 END
FROM seed_seq;

INSERT INTO service_project (
    id, name, category, price, duration_minutes, description, status, create_time, update_time
)
SELECT
    1000 + n,
    CONCAT('Service Project ', LPAD(n, 3, '0')),
    ELT(n % 5 + 1, 'face', 'body', 'skin', 'member', 'review'),
    58.00 + (n % 20) * 18.00,
    ELT(n % 6 + 1, 30, 45, 60, 75, 90, 120),
    'seed service project',
    CASE WHEN n % 14 = 0 THEN 0 ELSE 1 END,
    DATE_SUB(NOW(), INTERVAL n DAY),
    NOW()
FROM seed_seq;

INSERT INTO inventory_sku (
    id, name, category, unit, quantity, safety_stock, cost_price, supplier, status, remark, create_time, update_time
)
SELECT
    1000 + n,
    CONCAT('Inventory SKU ', LPAD(n, 3, '0')),
    ELT(n % 5 + 1, 'material', 'consumable', 'product', 'card', 'device_part'),
    ELT(n % 6 + 1, 'box', 'pack', 'bottle', 'sheet', 'piece', 'set'),
    (n * 3) % 120,
    10 + n % 15,
    8.00 + (n % 30) * 2.00,
    ELT(n % 5 + 1, 'supplier_a', 'supplier_b', 'supplier_c', 'supplier_d', 'supplier_e'),
    CASE WHEN n % 18 = 0 THEN 0 ELSE 1 END,
    CASE WHEN ((n * 3) % 120) < (10 + n % 15) THEN 'below safety stock' ELSE 'normal stock' END,
    DATE_SUB(NOW(), INTERVAL n DAY),
    NOW()
FROM seed_seq;

INSERT INTO service_project_inventory (
    id, service_project_id, inventory_id, consume_quantity, status, remark, create_time, update_time
)
SELECT
    1000 + n,
    1000 + n,
    1000 + ((n * 7) % 100) + 1,
    1 + n % 3,
    CASE WHEN n % 16 = 0 THEN 0 ELSE 1 END,
    'seed service project inventory',
    DATE_SUB(NOW(), INTERVAL n DAY),
    NOW()
FROM seed_seq;

INSERT INTO appointment (
    id, appointment_no, customer_id, staff_id, appointment_time, status,
    total_duration_minutes, remark, create_time, update_time, deleted
)
SELECT
    1000 + n,
    CONCAT('APT20260704', 1000 + n),
    1000 + n,
    1000 + (n % 100) + 1,
    DATE_ADD(
        DATE_ADD(CURDATE(), INTERVAL ((n % 21) - 10) DAY),
        INTERVAL (540 + (n % 10) * 60) MINUTE
    ),
    n % 4,
    ELT(n % 5 + 1, 30, 60, 90, 120, 150),
    'seed appointment',
    DATE_SUB(NOW(), INTERVAL n DAY),
    NOW(),
    CASE WHEN n % 30 = 0 THEN 1 ELSE 0 END
FROM seed_seq;

INSERT INTO appointment_item (
    id, appointment_id, service_project_id, staff_id, service_name, price,
    duration_minutes, sort_no, create_time
)
SELECT
    1000 + n,
    1000 + n,
    1000 + ((n * 3) % 100) + 1,
    CASE WHEN n % 9 = 0 THEN NULL ELSE 1000 + ((n * 2) % 100) + 1 END,
    CONCAT('Appointment Item ', LPAD(n, 3, '0')),
    68.00 + (n % 25) * 12.00,
    ELT(n % 6 + 1, 30, 45, 60, 75, 90, 120),
    n % 3 + 1,
    DATE_SUB(NOW(), INTERVAL n DAY)
FROM seed_seq;

INSERT INTO service_order (
    id, order_no, appointment_id, customer_id, order_type, original_amount,
    discount_amount, receivable_amount, paid_amount, debt_amount, debt_status, pay_status,
    order_status, remark, create_time, update_time, deleted
)
SELECT
    1000 + n,
    CONCAT('ORD20260704', 1000 + n),
    CASE WHEN n % 7 = 0 THEN NULL ELSE 1000 + n END,
    1000 + n,
    ELT(n % 5 + 1, 'service', 'time_card', 'care_card', 'member_card', 'course_card'),
    120.00 + (n % 30) * 20.00,
    CASE WHEN n % 5 = 0 THEN 30.00 ELSE 0.00 END,
    120.00 + (n % 30) * 20.00 - CASE WHEN n % 5 = 0 THEN 30.00 ELSE 0.00 END,
    CASE
        WHEN n % 4 = 0 THEN 0.00
        WHEN n % 4 = 1 THEN ROUND((120.00 + (n % 30) * 20.00 - CASE WHEN n % 5 = 0 THEN 30.00 ELSE 0.00 END) / 2, 2)
        WHEN n % 4 = 2 THEN 120.00 + (n % 30) * 20.00 - CASE WHEN n % 5 = 0 THEN 30.00 ELSE 0.00 END
        ELSE 0.00
    END,
    CASE
        WHEN n % 4 = 0 THEN 120.00 + (n % 30) * 20.00 - CASE WHEN n % 5 = 0 THEN 30.00 ELSE 0.00 END
        WHEN n % 4 = 1 THEN ROUND((120.00 + (n % 30) * 20.00 - CASE WHEN n % 5 = 0 THEN 30.00 ELSE 0.00 END) / 2, 2)
        ELSE 0.00
    END,
    CASE
        WHEN n % 4 IN (0, 1) THEN 1
        WHEN n % 4 = 2 THEN 2
        ELSE 0
    END,
    n % 4,
    n % 3,
    'seed service order',
    DATE_SUB(NOW(), INTERVAL n DAY),
    NOW(),
    CASE WHEN n % 40 = 0 THEN 1 ELSE 0 END
FROM seed_seq;

INSERT INTO service_order_item (
    id, order_id, service_project_id, service_name, unit_price, quantity,
    discount_amount, actual_amount, staff_id, remark, create_time
)
SELECT
    1000 + n,
    1000 + n,
    CASE WHEN n % 11 = 0 THEN NULL ELSE 1000 + ((n * 5) % 100) + 1 END,
    CONCAT('Order Item ', LPAD(n, 3, '0')),
    80.00 + (n % 18) * 15.00,
    n % 3 + 1,
    CASE WHEN n % 6 = 0 THEN 10.00 ELSE 0.00 END,
    (80.00 + (n % 18) * 15.00) * (n % 3 + 1) - CASE WHEN n % 6 = 0 THEN 10.00 ELSE 0.00 END,
    1000 + ((n * 3) % 100) + 1,
    'seed service order item',
    DATE_SUB(NOW(), INTERVAL n DAY)
FROM seed_seq;

INSERT INTO payment_record (
    id, order_id, payment_no, payment_method, pay_amount, pay_status,
    pay_time, operator_id, remark, create_time
)
SELECT
    1000 + n,
    1000 + n,
    CONCAT('PAY20260704', 1000 + n),
    ELT(n % 4 + 1, 'wechat', 'alipay', 'cash', 'time_card'),
    20.00 + (n % 20) * 15.00,
    n % 4,
    CASE WHEN n % 4 = 0 THEN NULL ELSE DATE_SUB(NOW(), INTERVAL n DAY) END,
    1000 + (n % 3) + 1,
    'seed payment record',
    DATE_SUB(NOW(), INTERVAL n DAY)
FROM seed_seq;

INSERT INTO inventory_stock_log (
    id, inventory_id, change_type, change_quantity, before_quantity,
    after_quantity, related_order_id, operator_id, remark, create_time
)
SELECT
    1000 + n,
    1000 + n,
    ELT(n % 5 + 1, 'stock_in', 'stock_out', 'check', 'loss', 'return'),
    1 + n % 10,
    30.00 + n % 60,
    30.00 + n % 60 + CASE
        WHEN n % 5 IN (1, 3) THEN -(1 + n % 10)
        WHEN n % 5 = 2 THEN CASE WHEN n % 2 = 0 THEN 1 + n % 10 ELSE -(1 + n % 10) END
        ELSE 1 + n % 10
    END,
    CASE WHEN n % 4 = 0 THEN NULL ELSE 1000 + n END,
    1000 + (n % 3) + 1,
    'seed inventory stock log',
    DATE_SUB(NOW(), INTERVAL n DAY)
FROM seed_seq;

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'sys_user' AS table_name, COUNT(*) AS seed_rows FROM sys_user WHERE id BETWEEN 1001 AND 1100
UNION ALL SELECT 'staff_member', COUNT(*) FROM staff_member WHERE id BETWEEN 1001 AND 1100
UNION ALL SELECT 'customer_profile', COUNT(*) FROM customer_profile WHERE id BETWEEN 1001 AND 1100
UNION ALL SELECT 'service_project', COUNT(*) FROM service_project WHERE id BETWEEN 1001 AND 1100
UNION ALL SELECT 'inventory_sku', COUNT(*) FROM inventory_sku WHERE id BETWEEN 1001 AND 1100
UNION ALL SELECT 'service_project_inventory', COUNT(*) FROM service_project_inventory WHERE id BETWEEN 1001 AND 1100
UNION ALL SELECT 'appointment', COUNT(*) FROM appointment WHERE id BETWEEN 1001 AND 1100
UNION ALL SELECT 'appointment_item', COUNT(*) FROM appointment_item WHERE id BETWEEN 1001 AND 1100
UNION ALL SELECT 'service_order', COUNT(*) FROM service_order WHERE id BETWEEN 1001 AND 1100
UNION ALL SELECT 'service_order_item', COUNT(*) FROM service_order_item WHERE id BETWEEN 1001 AND 1100
UNION ALL SELECT 'payment_record', COUNT(*) FROM payment_record WHERE id BETWEEN 1001 AND 1100
UNION ALL SELECT 'inventory_stock_log', COUNT(*) FROM inventory_stock_log WHERE id BETWEEN 1001 AND 1100;

DROP TEMPORARY TABLE IF EXISTS seed_seq;
