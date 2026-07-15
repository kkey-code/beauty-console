package com.wkr.storepojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 工作台聚合响应，使用一次请求返回指标和三个重点列表。
 */
@Data
@ApiModel(value = "DashboardOverviewVO", description = "工作台经营概览")
public class DashboardOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("客户总数")
    private long customerTotal;

    @ApiModelProperty("预约总数")
    private long appointmentTotal;

    @ApiModelProperty("订单总数")
    private long orderTotal;

    @ApiModelProperty("库存耗材总数")
    private long inventoryTotal;

    @ApiModelProperty("待服务订单")
    private List<ServiceOrderVO> pendingOrders = new ArrayList<>();

    @ApiModelProperty("近期预约")
    private List<AppointmentVO> appointments = new ArrayList<>();

    @ApiModelProperty("低库存耗材")
    private List<InventorySkuVO> lowStockItems = new ArrayList<>();
}
