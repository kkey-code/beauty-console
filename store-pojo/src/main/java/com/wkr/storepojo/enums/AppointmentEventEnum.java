package com.wkr.storepojo.enums;

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
