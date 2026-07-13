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
import java.time.LocalDateTime;

/**
 * 订单收付款参数，承载支付方式、金额和流水状态等信息。
 */
@Data
@ApiModel(value = "OrderPaymentDTO", description = "订单收款参数")
public class OrderPaymentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "订单ID不能为空")
    @ApiModelProperty("订单ID")
    private Long orderId;

    @NotBlank(message = "支付方式不能为空")
    @Size(max = 20, message = "支付方式不能超过20个字符")
    @ApiModelProperty("支付方式：wechat/alipay/cash/time_card")
    private String paymentMethod;

    @NotNull(message = "收款金额不能为空")
    @DecimalMin(value = "0.01", message = "收款金额必须大于0")
    @ApiModelProperty("收款金额")
    private BigDecimal payAmount;

    @ApiModelProperty("收款时间")
    private LocalDateTime payTime;

    @Size(max = 500, message = "备注不能超过500个字符")
    @ApiModelProperty("备注")
    private String remark;
}
