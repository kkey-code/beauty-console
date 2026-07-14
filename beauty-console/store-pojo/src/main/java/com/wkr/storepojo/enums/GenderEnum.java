package com.wkr.storepojo.enums;

/**
 * 性别枚举，统一性别字段的存储值和展示名称。
 */
public enum GenderEnum {

    MALE(1, "男"),
    FEMALE(2, "女");

    private final int code;
    private final String label;

    GenderEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean matches(Integer value) {
        return value != null && value == code;
    }

    public static String labelOf(Integer value) {
        for (GenderEnum item : values()) {
            if (item.matches(value)) {
                return item.label;
            }
        }
        return "未知";
    }
}
