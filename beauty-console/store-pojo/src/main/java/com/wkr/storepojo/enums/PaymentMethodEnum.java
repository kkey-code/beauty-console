package com.wkr.storepojo.enums;

/**
 * 支付方式枚举，统一微信、支付宝、现金和卡项等支付渠道。
 */
public enum PaymentMethodEnum {

    WECHAT("wechat", "微信"),
    ALIPAY("alipay", "支付宝"),
    CASH("cash", "现金"),
    TIME_CARD("time_card", "次卡");

    private final String code;
    private final String label;

    PaymentMethodEnum(String code, String label) {
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
        for (PaymentMethodEnum item : values()) {
            if (item.matches(value)) {
                return item.label;
            }
        }
        return "未知";
    }
}
