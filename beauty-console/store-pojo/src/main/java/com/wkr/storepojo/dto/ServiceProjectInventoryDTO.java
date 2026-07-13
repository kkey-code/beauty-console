package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 服务项目耗材关系请求参数，声明项目、库存 SKU 和默认消耗数量的保存校验规则。
 */
@Data
@ApiModel(value = "ServiceProjectInventoryDTO", description = "服务项目耗材关系保存参数")
public class ServiceProjectInventoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键，新增时为空")
    private Long id;

    @NotNull(message = "服务项目ID不能为空")
    @ApiModelProperty("服务项目ID")
    private Long serviceProjectId;

    @NotNull(message = "库存物品ID不能为空")
    @ApiModelProperty("库存物品ID")
    private Long inventoryId;

    @NotNull(message = "消耗数量不能为空")
    @DecimalMin(value = "0.01", message = "消耗数量必须大于0")
    @ApiModelProperty("单次服务默认消耗数量")
    private BigDecimal consumeQuantity;

    @NotNull(message = "状态不能为空")
    @ApiModelProperty("状态：0停用，1启用")
    private Integer status;

    @Size(max = 500, message = "备注不能超过500个字符")
    @ApiModelProperty("备注")
    private String remark;
}
