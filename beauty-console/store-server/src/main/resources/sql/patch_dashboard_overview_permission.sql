-- Existing environments: authorize the new aggregated dashboard API.
USE db_platform;

SET NAMES utf8mb4;

UPDATE sys_permission
SET method = 'GET',
    path_pattern = '/admin/dashboard/**',
    update_time = NOW()
WHERE permission_code = 'dashboard:view';
