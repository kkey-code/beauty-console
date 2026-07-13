CREATE TABLE IF NOT EXISTS operation_audit_log (
    id                BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    operator_user_id  BIGINT NULL COMMENT 'Operator account ID',
    operator_staff_id BIGINT NULL COMMENT 'Operator staff ID',
    action_type       VARCHAR(64) NOT NULL COMMENT 'Action type',
    target_type       VARCHAR(64) NOT NULL COMMENT 'Target type',
    target_id         BIGINT NULL COMMENT 'Target ID',
    detail            VARCHAR(500) NULL COMMENT 'Detail',
    create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_audit_operator (operator_user_id),
    KEY idx_audit_target (target_type, target_id),
    KEY idx_audit_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Operation audit log';

SET @idx_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'appointment'
      AND INDEX_NAME = 'uk_appointment_no'
);
SET @sql := IF(@idx_exists = 0,
    'ALTER TABLE appointment ADD UNIQUE KEY uk_appointment_no (appointment_no)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'service_order'
      AND INDEX_NAME = 'uk_service_order_no'
);
SET @sql := IF(@idx_exists = 0,
    'ALTER TABLE service_order ADD UNIQUE KEY uk_service_order_no (order_no)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'payment_record'
      AND INDEX_NAME = 'uk_payment_no'
);
SET @sql := IF(@idx_exists = 0,
    'ALTER TABLE payment_record ADD UNIQUE KEY uk_payment_no (payment_no)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
