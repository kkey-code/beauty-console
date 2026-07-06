package com.wkr.storepojo.enums;

public enum OrderTypeEnum {

    SERVICE("service", "服务"),
    TIME_CARD("time_card", "次卡"),
    CARE_CARD("care_card", "护理卡"),
    MEMBER_CARD("member_card", "会员卡"),
    COURSE_CARD("course_card", "疗程卡");

    private final String code;
    private final String label;

    OrderTypeEnum(String code, String label) {
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
        for (OrderTypeEnum item : values()) {
            if (item.matches(value)) {
                return item.label;
            }
        }
        return "未知";
    }
}
