package com.wkr.storepojo.enums;

public enum RoleCodeEnum {

    ADMIN("admin", "管理员"),
    STAFF("staff", "员工"),
    READONLY("readonly", "只读账号");

    private final String code;
    private final String label;

    RoleCodeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean matches(String value) {
        return value != null && code.equalsIgnoreCase(value);
    }

    public static String labelOf(String value) {
        for (RoleCodeEnum item : values()) {
            if (item.matches(value)) {
                return item.label;
            }
        }
        return "未知";
    }
}
