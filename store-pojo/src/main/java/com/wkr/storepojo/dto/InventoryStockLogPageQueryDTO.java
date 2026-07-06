package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "InventoryStockLogPageQueryDTO", description = "库存流水分页查询参数")
public class InventoryStockLogPageQueryDTO extends PageQueryDTO {

    @ApiModelProperty("库存物品ID")
    private Long inventoryId;

    @ApiModelProperty("变动类型")
    private String changeType;

    @ApiModelProperty("关联订单ID")
    private Long relatedOrderId;

    @ApiModelProperty("开始时间")
    private LocalDateTime beginTime;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;
}
