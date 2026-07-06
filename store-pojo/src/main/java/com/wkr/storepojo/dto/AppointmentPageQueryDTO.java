package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "AppointmentPageQueryDTO", description = "预约分页查询参数")
public class AppointmentPageQueryDTO extends PageQueryDTO {

    @ApiModelProperty("客户姓名")
    private String customerName;

    @ApiModelProperty("员工姓名")
    private String staffName;

    @ApiModelProperty("预约状态")
    private Integer status;

    @ApiModelProperty("开始时间")
    private LocalDateTime beginTime;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;
}
