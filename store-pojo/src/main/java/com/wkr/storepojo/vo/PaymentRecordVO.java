package com.wkr.storepojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收款流水响应对象，组合接口返回给前端展示的业务字段。
 */
@Data
@ApiModel(value = "PaymentRecordVO", description = "收款流水展示对象")
public class PaymentRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("收款流水号")
    private String paymentNo;

    @ApiModelProperty("支付方式")
    private String paymentMethod;

    @ApiModelProperty("支付方式名称")
    private String paymentMethodName;

    @ApiModelProperty("收款金额")
    private BigDecimal payAmount;

    @ApiModelProperty("状态")
    private Integer payStatus;

    @ApiModelProperty("状态名称")
    private String payStatusName;

    @ApiModelProperty("收款时间")
    private LocalDateTime payTime;

    @ApiModelProperty("操作人ID")
    private Long operatorId;

    @ApiModelProperty("操作人姓名")
    private String operatorName;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
