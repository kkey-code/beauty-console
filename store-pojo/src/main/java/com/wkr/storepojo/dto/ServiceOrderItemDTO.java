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
 * 订单项目明细请求参数，承载新增或修改接口的数据并声明输入校验规则。
 */
@Data
@ApiModel(value = "ServiceOrderItemDTO", description = "订单项目明细参数")
public class ServiceOrderItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键，新增时为空")
    private Long id;

    @ApiModelProperty("订单ID")
    private Long orderId;

    @NotNull(message = "服务项目ID不能为空")
    @ApiModelProperty("服务项目ID")
    private Long serviceProjectId;

    @Size(max = 100, message = "项目名称不能超过100个字符")
    @ApiModelProperty("项目快照名称")
    private String serviceName;

    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.00", message = "单价不能小于0")
    @ApiModelProperty("单价")
    private BigDecimal unitPrice;

    @NotNull(message = "数量不能为空")
    @DecimalMin(value = "0.01", message = "数量必须大于0")
    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @DecimalMin(value = "0.00", message = "优惠金额不能小于0")
    @ApiModelProperty("明细优惠金额")
    private BigDecimal discountAmount;

    @NotNull(message = "明细应收金额不能为空")
    @DecimalMin(value = "0.00", message = "明细应收金额不能小于0")
    @ApiModelProperty("明细应收金额")
    private BigDecimal actualAmount;

    @ApiModelProperty("服务员工ID")
    private Long staffId;

    @Size(max = 500, message = "备注不能超过500个字符")
    @ApiModelProperty("备注")
    private String remark;
}
