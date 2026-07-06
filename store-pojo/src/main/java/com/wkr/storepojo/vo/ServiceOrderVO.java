package com.wkr.storepojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(value = "ServiceOrderVO", description = "订单展示对象")
public class ServiceOrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("关联预约ID")
    private Long appointmentId;

    @ApiModelProperty("客户ID")
    private Long customerId;

    @ApiModelProperty("客户姓名")
    private String customerName;

    @ApiModelProperty("客户手机号")
    private String customerPhone;

    @ApiModelProperty("订单类型")
    private String orderType;

    @ApiModelProperty("订单类型名称")
    private String orderTypeName;

    @ApiModelProperty("原价金额")
    private BigDecimal originalAmount;

    @ApiModelProperty("优惠金额")
    private BigDecimal discountAmount;

    @ApiModelProperty("应收金额")
    private BigDecimal receivableAmount;

    @ApiModelProperty("已收金额")
    private BigDecimal paidAmount;

    @ApiModelProperty("欠款金额")
    private BigDecimal debtAmount;

    @ApiModelProperty("欠款状态")
    private Integer debtStatus;

    @ApiModelProperty("欠款状态名称")
    private String debtStatusName;

    @ApiModelProperty("支付状态")
    private Integer payStatus;

    @ApiModelProperty("支付状态名称")
    private String payStatusName;

    @ApiModelProperty("订单状态")
    private Integer orderStatus;

    @ApiModelProperty("订单状态名称")
    private String orderStatusName;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("订单项目明细")
    private List<ServiceOrderItemVO> items;

    @ApiModelProperty("收款流水")
    private List<PaymentRecordVO> payments;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
