package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 服务订单分页查询参数，封装页码以及该模块支持的筛选条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "ServiceOrderPageQueryDTO", description = "订单分页查询参数")
public class ServiceOrderPageQueryDTO extends PageQueryDTO {

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("客户姓名")
    private String customerName;

    @ApiModelProperty("订单类型")
    private String orderType;

    @ApiModelProperty("支付状态")
    private Integer payStatus;

    @ApiModelProperty("欠款状态")
    private Integer debtStatus;

    @ApiModelProperty("订单状态")
    private Integer orderStatus;

    @ApiModelProperty("开始时间")
    private LocalDateTime beginTime;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;
}
