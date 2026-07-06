package com.wkr.storepojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel(value = "InventoryStockLogVO", description = "库存流水展示对象")
public class InventoryStockLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("库存物品ID")
    private Long inventoryId;

    @ApiModelProperty("库存名称")
    private String inventoryName;

    @ApiModelProperty("变动类型")
    private String changeType;

    @ApiModelProperty("变动类型名称")
    private String changeTypeName;

    @ApiModelProperty("变动数量")
    private BigDecimal changeQuantity;

    @ApiModelProperty("变动前库存")
    private BigDecimal beforeQuantity;

    @ApiModelProperty("变动后库存")
    private BigDecimal afterQuantity;

    @ApiModelProperty("关联订单ID")
    private Long relatedOrderId;

    @ApiModelProperty("操作人ID")
    private Long operatorId;

    @ApiModelProperty("操作人姓名")
    private String operatorName;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
