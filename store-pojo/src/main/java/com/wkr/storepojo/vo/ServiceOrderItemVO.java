package com.wkr.storepojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单项目明细响应对象，组合接口返回给前端展示的业务字段。
 */
@Data
@ApiModel(value = "ServiceOrderItemVO", description = "订单项目明细展示对象")
public class ServiceOrderItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("服务项目ID")
    private Long serviceProjectId;

    @ApiModelProperty("项目快照名称")
    private String serviceName;

    @ApiModelProperty("单价")
    private BigDecimal unitPrice;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("明细优惠金额")
    private BigDecimal discountAmount;

    @ApiModelProperty("明细应收金额")
    private BigDecimal actualAmount;

    @ApiModelProperty("服务员工ID")
    private Long staffId;

    @ApiModelProperty("服务员工姓名")
    private String staffName;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
