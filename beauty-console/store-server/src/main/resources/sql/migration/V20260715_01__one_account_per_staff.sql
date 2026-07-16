-- Existing environments: enforce one login account per employee.
-- Check and resolve duplicate non-null staff_id rows before running this patch.

SET @has_unique_staff_index := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND index_name = 'uk_sys_user_staff'
);

SET @sql := IF(
    @has_unique_staff_index = 0,
    'ALTER TABLE sys_user ADD UNIQUE KEY uk_sys_user_staff (staff_id)',
    'SELECT ''uk_sys_user_staff already exists'''
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
