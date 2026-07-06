package com.wkr.storepojo.enums;

public enum PaymentRecordStatusEnum {

    UNCONFIRMED(0, "未确认"),
    SUCCESS(1, "成功"),
    REFUNDED(2, "退款"),
    VOIDED(3, "作废");

    private final int code;
    private final String label;

    PaymentRecordStatusEnum(int code, String label) {
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
        for (PaymentRecordStatusEnum item : values()) {
            if (item.matches(value)) {
                return item.label;
            }
        }
        return "未知";
    }
}
