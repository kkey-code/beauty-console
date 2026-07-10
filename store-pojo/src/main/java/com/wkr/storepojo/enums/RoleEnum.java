package com.wkr.storepojo.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统角色枚举，统一维护六类角色的数据库编号、稳定编码和展示名称。
 */
public enum RoleEnum {

    SUPER_ADMIN(1, "SUPER_ADMIN", "超级管理员"),
    STORE_MANAGER(2, "STORE_MANAGER", "店长"),
    STAFF(3, "STAFF", "普通员工"),
    INVENTORY_ADMIN(4, "INVENTORY_ADMIN", "库存管理员"),
    FINANCE(5, "FINANCE", "财务/收银"),
    READONLY(6, "READONLY", "只读");

    private final int code;
    private final String roleCode;
    private final String description;

    // 按 code（数字）查找
    private static final Map<Integer, RoleEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(e -> e.code, e -> e));

    // 按 roleCode（字符串）查找
    private static final Map<String, RoleEnum> ROLE_CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(e -> e.roleCode, e -> e));

    // 按 code 获取 description
    private static final Map<Integer, String> LABEL_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(e -> e.code, e -> e.description));

    RoleEnum(int code, String roleCode, String description) {
        this.code = code;
        this.roleCode = roleCode;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getRoleCode() { return roleCode; }
    public String getDescription() { return description; }

    // 通过 code（数字）获取枚举
    public static RoleEnum fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }

    // 通过 roleCode（字符串）获取枚举
    public static RoleEnum fromRoleCode(String roleCode) {
        return roleCode == null ? null : ROLE_CODE_MAP.get(roleCode.toUpperCase(Locale.ROOT));
    }

    // 通过 code（数字）获取 roleCode（字符串）
    public static String codeOf(Integer code) {
        RoleEnum role = fromCode(code);
        return role == null ? null : role.getRoleCode();
    }

    // 通过 code（数字）获取 description
    public static String labelOf(Integer code) {
        return code == null ? "未知" : LABEL_MAP.getOrDefault(code, "未知");
    }

    // 通过 roleCode（字符串）获取 description
    public static String labelOf(String roleCode) {
        RoleEnum role = fromRoleCode(roleCode);
        return role == null ? "未知" : role.getDescription();
    }

    // 判断当前枚举是否匹配指定的 code
    public boolean matches(Integer code) {
        return code != null && this.code == code;
    }

    // 判断当前枚举是否匹配指定的 roleCode
    public boolean matches(String roleCode) {
        return roleCode != null && this.roleCode.equalsIgnoreCase(roleCode);
    }

    // 权限等级比较（code 越小权限越高）
    public boolean isHigherThan(RoleEnum other) {
        return this.code < other.code;
    }
}
