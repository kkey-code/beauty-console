-- 旧数据库可能只保存了已废弃的工作台接口路径。
-- 保留权限编码不变，把匹配范围更新为当前聚合工作台接口及其子路径。

UPDATE sys_permission
SET method = 'GET',
    path_pattern = '/admin/dashboard/**',
    update_time = NOW()
WHERE permission_code = 'dashboard:view';
