package com.wkr.storepojo.enums;

/**
 * 欠款状态枚举，描述订单未欠款、欠款和结清等状态。
 */
public enum DebtStatusEnum {

    NONE(0, "无欠款"),
    INSTALLMENT(1, "分期中"),
    SETTLED(2, "已结清");

    private final int code;
    private final String label;

    DebtStatusEnum(int code, String label) {
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
        for (DebtStatusEnum item : values()) {
            if (item.matches(value)) {
                return item.label;
            }
        }
        return "未知";
    }
}
