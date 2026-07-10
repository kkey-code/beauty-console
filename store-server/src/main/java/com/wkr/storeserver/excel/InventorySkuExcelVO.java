package com.wkr.storeserver.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存物品 Excel 导出对象，定义 EasyExcel 输出列和展示字段。
 */
@Data
public class InventorySkuExcelVO {

    @ExcelProperty("库存ID")
    private Long id;

    @ExcelProperty("库存名称")
    private String name;

    @ExcelProperty("分类")
    private String category;

    @ExcelProperty("单位")
    private String unit;

    @ExcelProperty("当前库存")
    private BigDecimal quantity;

    @ExcelProperty("安全库存")
    private BigDecimal safetyStock;

    @ExcelProperty("是否低于安全库存")
    private String belowSafetyStock;

    @ExcelProperty("成本价")
    private BigDecimal costPrice;

    @ExcelProperty("供应商")
    private String supplier;

    @ExcelProperty("状态")
    private String statusName;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private String createTime;

    @ExcelProperty("更新时间")
    private String updateTime;
}
