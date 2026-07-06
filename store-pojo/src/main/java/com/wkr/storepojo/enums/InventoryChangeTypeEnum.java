package com.wkr.storepojo.enums;

public enum InventoryChangeTypeEnum {

    STOCK_IN("stock_in", "入库"),
    STOCK_OUT("stock_out", "出库"),
    CHECK("check", "盘点"),
    LOSS("loss", "报损"),
    RETURN("return", "退货");

    private final String code;
    private final String label;

    InventoryChangeTypeEnum(String code, String label) {
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
        for (InventoryChangeTypeEnum item : values()) {
            if (item.matches(value)) {
                return item.label;
            }
        }
        return "未知";
    }
}
