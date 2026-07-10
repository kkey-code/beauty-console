package com.wkr.storepojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存物品响应对象，组合接口返回给前端展示的业务字段。
 */
@Data
@ApiModel(value = "InventorySkuVO", description = "库存物品展示对象")
public class InventorySkuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("库存名称")
    private String name;

    @ApiModelProperty("分类")
    private String category;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("当前库存")
    private BigDecimal quantity;

    @ApiModelProperty("安全库存")
    private BigDecimal safetyStock;

    @ApiModelProperty("是否低于安全库存")
    private Boolean belowSafetyStock;

    @ApiModelProperty("成本价")
    private BigDecimal costPrice;

    @ApiModelProperty("供应商")
    private String supplier;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("状态名称")
    private String statusName;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
