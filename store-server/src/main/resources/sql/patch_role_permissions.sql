-- Migrate the legacy 3-role model to the 6-role model used by RoleEnum.
-- Verified legacy accounts:
--   admin    : 1 -> 1
--   staff    : 2 -> 3
--   readonly : 0/3 -> 6

SET @role_table_existed = (
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_role'
);

CREATE TABLE IF NOT EXISTS sys_role (
    id          INT         NOT NULL COMMENT 'Role ID',
    role_code   VARCHAR(50) NOT NULL COMMENT 'Stable role code',
    role_name   VARCHAR(50) NOT NULL COMMENT 'Role display name',
    description VARCHAR(255) DEFAULT NULL COMMENT 'Role description',
    status      TINYINT     NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System roles';

INSERT INTO sys_role (id, role_code, role_name, description, status)
VALUES
    (1, 'SUPER_ADMIN',    '超级管理员', 'All administrative permissions', 1),
    (2, 'STORE_MANAGER',  '店长',       'Store operations and staff management', 1),
    (3, 'STAFF',          '普通员工',   'Appointments, customers and service orders', 1),
    (4, 'INVENTORY_ADMIN','库存管理员', 'Inventory and stock movement management', 1),
    (5, 'FINANCE',        '财务/收银',  'Payment and financial operations', 1),
    (6, 'READONLY',       '只读',       'Read-only access', 1)
ON DUPLICATE KEY UPDATE
    role_code = VALUES(role_code),
    role_name = VALUES(role_name),
    description = VALUES(description),
    status = VALUES(status);

UPDATE sys_user
SET role_id = 6
WHERE role_id = 0;

UPDATE sys_user
SET role_id = 3
WHERE username = 'staff'
  AND role_id = 2;

UPDATE sys_user
SET role_id = 6
WHERE username = 'readonly'
  AND role_id = 3;

UPDATE sys_user
SET role_id = 3
WHERE @role_table_existed = 0
  AND role_id = 2;

UPDATE sys_user
SET role_id = 6
WHERE @role_table_existed = 0
  AND role_id = 3;

UPDATE sys_user
SET role_id = 6
WHERE role_id IS NULL
   OR role_id NOT IN (1, 2, 3, 4, 5, 6);

SET @role_index_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND index_name = 'idx_role_id'
);
SET @role_index_sql = IF(
    @role_index_exists = 0,
    'ALTER TABLE sys_user ADD INDEX idx_role_id (role_id)',
    'SELECT 1'
);
PREPARE role_index_stmt FROM @role_index_sql;
EXECUTE role_index_stmt;
DEALLOCATE PREPARE role_index_stmt;

SET @role_fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'sys_user'
      AND constraint_name = 'fk_sys_user_role'
      AND constraint_type = 'FOREIGN KEY'
);
SET @role_fk_sql = IF(
    @role_fk_exists = 0,
    'ALTER TABLE sys_user ADD CONSTRAINT fk_sys_user_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON UPDATE RESTRICT ON DELETE RESTRICT',
    'SELECT 1'
);
PREPARE role_fk_stmt FROM @role_fk_sql;
EXECUTE role_fk_stmt;
DEALLOCATE PREPARE role_fk_stmt;
