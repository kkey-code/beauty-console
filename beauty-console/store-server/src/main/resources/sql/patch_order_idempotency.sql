-- 订单幂等升级：请求 UUID 唯一，同一预约最多生成一张订单。
-- 执行唯一索引前应先检查并清理历史重复 appointment_id 数据。

SET @column_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'service_order'
      AND COLUMN_NAME = 'request_id'
);
SET @sql := IF(@column_exists = 0,
    'ALTER TABLE service_order ADD COLUMN request_id VARCHAR(36) NULL COMMENT ''下单幂等请求ID'' AFTER order_no',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'service_order'
      AND INDEX_NAME = 'uk_service_order_request_id'
);
SET @sql := IF(@idx_exists = 0,
    'ALTER TABLE service_order ADD UNIQUE KEY uk_service_order_request_id (request_id)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'service_order'
      AND INDEX_NAME = 'uk_service_order_appointment'
);
SET @sql := IF(@idx_exists = 0,
    'ALTER TABLE service_order ADD UNIQUE KEY uk_service_order_appointment (appointment_id)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
