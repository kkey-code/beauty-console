package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "InventorySkuPageQueryDTO", description = "库存物品分页查询参数")
public class InventorySkuPageQueryDTO extends PageQueryDTO {

    @ApiModelProperty("库存名称")
    private String name;

    @ApiModelProperty("分类")
    private String category;

    @ApiModelProperty("状态：0停用，1启用")
    private Integer status;

    @ApiModelProperty("是否只查低于安全库存的物品")
    private Boolean lowStockOnly;
}
