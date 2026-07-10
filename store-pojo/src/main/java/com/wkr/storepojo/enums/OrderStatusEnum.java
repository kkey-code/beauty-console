package com.wkr.storepojo.enums;

import com.wkr.storecommon.exception.BusinessException;

/**
 * 订单状态枚举，统一订单生命周期状态并提供状态转换校验。
 */
public enum OrderStatusEnum {

    PENDING(0, "待服务"),
    COMPLETED(1, "已完成"),
    CANCELED(2, "已取消");

    private final int code;
    private final String label;

    OrderStatusEnum(int code, String label) {
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
        for (OrderStatusEnum item : values()) {
            if (item.matches(value)) {
                return item.label;
            }
        }
        return "未知";
    }
    public static boolean isValid(Integer code) {
        if (code == null) return false;
        for (OrderStatusEnum item : values()) {
            if (item.code == code) {
                return true;
            }
        }
        return false;
    }
    public static void validate(Integer code) {
        if (!isValid(code)) {
            throw new BusinessException("订单状态错误");
        }
    }
}
