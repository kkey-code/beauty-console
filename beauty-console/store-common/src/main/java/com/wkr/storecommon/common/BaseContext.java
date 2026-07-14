package com.wkr.storecommon.common;

/**
 * 请求级用户上下文，使用 ThreadLocal 保存当前登录用户、员工和角色信息，并在请求结束时统一清理。
 */
public final class BaseContext {

    private static final ThreadLocal<CurrentUser> CURRENT_USER = new ThreadLocal<>();

    private BaseContext() {
    }

    public static void setCurrentId(Long staffId) {
        CurrentUser currentUser = CURRENT_USER.get();
        if (currentUser == null) {
            CURRENT_USER.set(new CurrentUser(null, staffId, null, null));
            return;
        }
        CURRENT_USER.set(new CurrentUser(
                currentUser.userId(),
                staffId,
                currentUser.roleId(),
                currentUser.roleCode()));
    }

    public static Long getCurrentId() {
        CurrentUser currentUser = CURRENT_USER.get();
        return currentUser == null ? null : currentUser.staffId();
    }

    public static Long getCurrentUserId() {
        CurrentUser currentUser = CURRENT_USER.get();
        return currentUser == null ? null : currentUser.userId();
    }

    public static Integer getCurrentRoleId() {
        CurrentUser currentUser = CURRENT_USER.get();
        return currentUser == null ? null : currentUser.roleId();
    }

    public static String getCurrentRole() {
        CurrentUser currentUser = CURRENT_USER.get();
        return currentUser == null ? null : currentUser.roleCode();
    }

    public static CurrentUser getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void setCurrentUser(Long userId, Long staffId, Integer roleId, String roleCode) {
        CURRENT_USER.set(new CurrentUser(userId, staffId, roleId, roleCode));
    }

    public static void remove() {
        CURRENT_USER.remove();
    }

    public record CurrentUser(Long userId, Long staffId, Integer roleId, String roleCode) {
    }
}
