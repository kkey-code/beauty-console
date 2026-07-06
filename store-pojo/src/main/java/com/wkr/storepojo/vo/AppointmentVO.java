package com.wkr.storepojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(value = "AppointmentVO", description = "预约展示对象")
public class AppointmentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("预约编号")
    private String appointmentNo;

    @ApiModelProperty("客户ID")
    private Long customerId;

    @ApiModelProperty("客户姓名")
    private String customerName;

    @ApiModelProperty("客户手机号")
    private String customerPhone;

    @ApiModelProperty("主服务员工ID")
    private Long staffId;

    @ApiModelProperty("主服务员工姓名")
    private String staffName;

    @ApiModelProperty("预约时间")
    private LocalDateTime appointmentTime;

    @ApiModelProperty("状态：0待确认，1已确认，2已完成，3已取消")
    private Integer status;

    @ApiModelProperty("状态名称")
    private String statusName;

    @ApiModelProperty("预计总时长，单位分钟")
    private Integer totalDurationMinutes;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("预约项目明细")
    private List<AppointmentItemVO> items;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
