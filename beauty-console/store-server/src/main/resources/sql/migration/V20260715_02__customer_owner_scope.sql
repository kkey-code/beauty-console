-- Preserve the employee who created a customer so a newly created customer
-- remains visible before the first appointment or service order is created.

SET @has_owner_staff_column := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'customer_profile'
      AND column_name = 'owner_staff_id'
);

SET @sql := IF(
    @has_owner_staff_column = 0,
    'ALTER TABLE customer_profile ADD COLUMN owner_staff_id BIGINT NULL COMMENT ''建档员工ID，用于员工数据范围'' AFTER source',
    'SELECT ''customer_profile.owner_staff_id already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_owner_staff_index := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'customer_profile'
      AND index_name = 'idx_customer_owner_staff'
);

SET @sql := IF(
    @has_owner_staff_index = 0,
    'ALTER TABLE customer_profile ADD INDEX idx_customer_owner_staff (owner_staff_id)',
    'SELECT ''idx_customer_owner_staff already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
