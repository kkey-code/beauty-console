package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务项目耗材关系分页查询参数，按服务项目、库存 SKU 和启停状态筛选配置。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "ServiceProjectInventoryPageQueryDTO", description = "服务项目耗材关系分页查询参数")
public class ServiceProjectInventoryPageQueryDTO extends PageQueryDTO {

    @ApiModelProperty("服务项目ID")
    private Long serviceProjectId;

    @ApiModelProperty("库存物品ID")
    private Long inventoryId;

    @ApiModelProperty("状态：0停用，1启用")
    private Integer status;
}
