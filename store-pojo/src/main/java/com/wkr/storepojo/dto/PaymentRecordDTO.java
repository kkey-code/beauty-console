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
 * 收款流水请求参数，承载新增或修改接口的数据并声明输入校验规则。
 */
@Data
@ApiModel(value = "PaymentRecordDTO", description = "收款流水保存参数")
public class PaymentRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键，新增时为空")
    private Long id;

    @NotNull(message = "订单ID不能为空")
    @ApiModelProperty("订单ID")
    private Long orderId;

    @Size(max = 64, message = "收款流水号不能超过64个字符")
    @ApiModelProperty("收款流水号")
    private String paymentNo;

    @NotBlank(message = "支付方式不能为空")
    @Size(max = 20, message = "支付方式不能超过20个字符")
    @ApiModelProperty("支付方式：wechat/alipay/cash/time_card")
    private String paymentMethod;

    @NotNull(message = "收款金额不能为空")
    @DecimalMin(value = "0.01", message = "收款金额必须大于0")
    @ApiModelProperty("收款金额")
    private BigDecimal payAmount;

    @NotNull(message = "收款状态不能为空")
    @ApiModelProperty("状态：0未确认，1成功，2退款，3作废")
    private Integer payStatus;

    @ApiModelProperty("收款时间")
    private LocalDateTime payTime;

    @ApiModelProperty("操作人ID")
    private Long operatorId;

    @Size(max = 500, message = "备注不能超过500个字符")
    @ApiModelProperty("备注")
    private String remark;
}
