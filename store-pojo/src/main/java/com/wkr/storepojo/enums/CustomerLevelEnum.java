package com.wkr.storepojo.enums;

public enum CustomerLevelEnum {

    NORMAL(0, "普通"),
    SILVER(1, "银卡"),
    GOLD(2, "金卡"),
    VIP(3, "VIP");

    private final Integer code;
    private final String label;

    CustomerLevelEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public Integer getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean matches(Integer  value) {
        return value != null && code.equals(value);
    }

    public static String labelOf(Integer value) {

        if (value == null) return "";

        for (CustomerLevelEnum item : values()) {
            if (item.matches(value)) {
                return item.label;
            }
        }
        return "" ;
    }
}
