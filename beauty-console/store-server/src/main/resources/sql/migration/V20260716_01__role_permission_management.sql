-- Add the role-default permission management endpoint without resetting any existing role defaults.

INSERT INTO sys_permission (
    permission_code, permission_name, permission_group, method,
    path_pattern, menu_path, action_key, status, create_time, update_time
) VALUES (
    'roles:permissions', '角色默认权限配置', '账号权限', '*',
    '/admin/permissions/roles/**', NULL, 'roles:permissions', 1, NOW(), NOW()
) ON DUPLICATE KEY UPDATE
    permission_name = VALUES(permission_name),
    method = VALUES(method),
    path_pattern = VALUES(path_pattern),
    status = VALUES(status),
    update_time = NOW();

INSERT INTO sys_permission (
    permission_code, permission_name, permission_group, method,
    path_pattern, menu_path, action_key, status, create_time, update_time
) VALUES (
    'users:resetPassword', '账号密码重置', '账号权限', 'PATCH',
    '/admin/users/*/reset-password', NULL, 'users:resetPassword', 1, NOW(), NOW()
) ON DUPLICATE KEY UPDATE
    permission_name = VALUES(permission_name),
    method = VALUES(method),
    path_pattern = VALUES(path_pattern),
    status = VALUES(status),
    update_time = NOW();

INSERT IGNORE INTO sys_role_permission (role_id, permission_id, create_time)
SELECT role_id, permission_id, NOW()
FROM (
    SELECT roles.role_id, permission.id AS permission_id
    FROM (
        SELECT 1 AS role_id
        UNION ALL
        SELECT 2 AS role_id
    ) roles
    JOIN sys_permission permission
      ON permission.permission_code IN ('roles:permissions', 'users:resetPassword')
) role_permission;
