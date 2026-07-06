package com.wkr.storepojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel(value = "AppointmentItemVO", description = "预约项目明细展示对象")
public class AppointmentItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("预约ID")
    private Long appointmentId;

    @ApiModelProperty("服务项目ID")
    private Long serviceProjectId;

    @ApiModelProperty("服务员工ID")
    private Long staffId;

    @ApiModelProperty("服务员工姓名")
    private String staffName;

    @ApiModelProperty("项目快照名称")
    private String serviceName;

    @ApiModelProperty("项目快照价格")
    private BigDecimal price;

    @ApiModelProperty("项目快照时长，单位分钟")
    private Integer durationMinutes;

    @ApiModelProperty("排序")
    private Integer sortNo;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
