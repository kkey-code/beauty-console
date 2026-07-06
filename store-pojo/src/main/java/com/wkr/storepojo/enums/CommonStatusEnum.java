package com.wkr.storepojo.enums;

public enum CommonStatusEnum {

    DISABLED(0, "停用"),
    ENABLED(1, "启用");

    private final int code;
    private final String label;

    CommonStatusEnum(int code, String label) {
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
        for (CommonStatusEnum item : values()) {
            if (item.matches(value)) {
                return item.label;
            }
        }
        return "未知";
    }
}
