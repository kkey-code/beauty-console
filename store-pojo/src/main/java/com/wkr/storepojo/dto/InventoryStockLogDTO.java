package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel(value = "InventoryStockLogDTO", description = "库存流水保存参数")
public class InventoryStockLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键，新增时为空")
    private Long id;

    @NotNull(message = "库存物品ID不能为空")
    @ApiModelProperty("库存物品ID")
    private Long inventoryId;

    @NotBlank(message = "库存变动类型不能为空")
    @Size(max = 20, message = "库存变动类型不能超过20个字符")
    @ApiModelProperty("类型：stock_in/stock_out/check/loss/return")
    private String changeType;

    @NotNull(message = "变动数量不能为空")
    @DecimalMin(value = "0.01", message = "变动数量必须大于0")
    @ApiModelProperty("变动数量")
    private BigDecimal changeQuantity;

    @ApiModelProperty("关联订单ID")
    private Long relatedOrderId;

    @ApiModelProperty("操作人ID")
    private Long operatorId;

    @Size(max = 500, message = "备注不能超过500个字符")
    @ApiModelProperty("备注")
    private String remark;
}
