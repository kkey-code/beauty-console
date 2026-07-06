package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel(value = "ServiceOrderDTO", description = "订单保存参数")
public class ServiceOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键，新增时为空")
    private Long id;

    @Size(max = 64, message = "订单编号不能超过64个字符")
    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("关联预约ID")
    private Long appointmentId;

    @NotNull(message = "客户ID不能为空")
    @ApiModelProperty("客户ID")
    private Long customerId;

    @NotBlank(message = "订单类型不能为空")
    @Size(max = 20, message = "订单类型不能超过20个字符")
    @ApiModelProperty("订单类型：service/time_card/care_card/member_card/course_card")
    private String orderType;

    @NotNull(message = "原价金额不能为空")
    @DecimalMin(value = "0.00", message = "原价金额不能小于0")
    @ApiModelProperty("原价金额")
    private BigDecimal originalAmount;

    @DecimalMin(value = "0.00", message = "优惠金额不能小于0")
    @ApiModelProperty("优惠金额")
    private BigDecimal discountAmount;

    @NotNull(message = "应收金额不能为空")
    @DecimalMin(value = "0.00", message = "应收金额不能小于0")
    @ApiModelProperty("应收金额")
    private BigDecimal receivableAmount;

    @DecimalMin(value = "0.00", message = "已收金额不能小于0")
    @ApiModelProperty("已收金额")
    private BigDecimal paidAmount;

    @DecimalMin(value = "0.00", message = "欠款金额不能小于0")
    @ApiModelProperty("欠款金额")
    private BigDecimal debtAmount;

    @ApiModelProperty("欠款状态：0无欠款，1分期中，2已结清")
    private Integer debtStatus;

    @ApiModelProperty("支付状态：0未支付，1部分支付，2已支付，3已退款")
    private Integer payStatus;

    @ApiModelProperty("订单状态：0待服务，1已完成，2已取消")
    private Integer orderStatus;

    @Size(max = 500, message = "备注不能超过500个字符")
    @ApiModelProperty("备注")
    private String remark;

    @Valid
    @Size(min = 1, message = "订单至少需要一个项目明细")
    @ApiModelProperty("订单项目明细")
    private List<ServiceOrderItemDTO> items;
}
