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

/**
 * 库存物品请求参数，承载新增或修改接口的数据并声明输入校验规则。
 */
@Data
@ApiModel(value = "InventorySkuDTO", description = "库存物品保存参数")
public class InventorySkuDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键，新增时为空")
    private Long id;

    @NotBlank(message = "库存名称不能为空")
    @Size(max = 64, message = "库存名称不能超过64个字符")
    @ApiModelProperty("库存名称")
    private String name;

    @Size(max = 64, message = "分类不能超过64个字符")
    @ApiModelProperty("分类")
    private String category;

    @Size(max = 20, message = "单位不能超过20个字符")
    @ApiModelProperty("单位")
    private String unit;

    @NotNull(message = "当前库存不能为空")
    @DecimalMin(value = "0.00", message = "当前库存不能小于0")
    @ApiModelProperty("当前库存")
    private BigDecimal quantity;

    @DecimalMin(value = "0.00", message = "安全库存不能小于0")
    @ApiModelProperty("安全库存")
    private BigDecimal safetyStock;

    @DecimalMin(value = "0.00", message = "成本价不能小于0")
    @ApiModelProperty("成本价")
    private BigDecimal costPrice;

    @Size(max = 100, message = "供应商不能超过100个字符")
    @ApiModelProperty("供应商")
    private String supplier;

    @NotNull(message = "状态不能为空")
    @ApiModelProperty("状态：0停用，1启用")
    private Integer status;

    @Size(max = 255, message = "备注不能超过255个字符")
    @ApiModelProperty("备注")
    private String remark;
}
