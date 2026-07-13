package com.wkr.storepojo.enums;

/**
 * 预约业务事件枚举，描述触发预约状态变化的操作类型。
 */
public enum AppointmentEventEnum {

    CONFIRM("确认"),
    COMPLETE("完成"),
    CANCEL("取消");

    private final String label;

    AppointmentEventEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }


}
