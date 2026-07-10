package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 收款流水分页查询参数，封装页码以及该模块支持的筛选条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "PaymentRecordPageQueryDTO", description = "收款流水分页查询参数")
public class PaymentRecordPageQueryDTO extends PageQueryDTO {

    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("支付方式")
    private String paymentMethod;

    @ApiModelProperty("收款状态")
    private Integer payStatus;

    @ApiModelProperty("开始时间")
    private LocalDateTime beginTime;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;
}
