package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约请求参数，承载新增或修改接口的数据并声明输入校验规则。
 */
@Data
@ApiModel(value = "AppointmentDTO", description = "预约保存参数")
public class AppointmentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键，新增时为空")
    private Long id;

    @Size(max = 64, message = "预约编号不能超过64个字符")
    @ApiModelProperty("预约编号")
    private String appointmentNo;

    @NotNull(message = "客户ID不能为空")
    @ApiModelProperty("客户ID")
    private Long customerId;

    @ApiModelProperty("主服务员工ID")
    private Long staffId;

    @NotNull(message = "预约时间不能为空")
    @ApiModelProperty("预约时间")
    private LocalDateTime appointmentTime;

    @NotNull(message = "预约状态不能为空")
    @ApiModelProperty("状态：0待确认，1已确认，2已完成，3已取消")
    private Integer status;

    @ApiModelProperty("预计总时长，单位分钟")
    private Integer totalDurationMinutes;

    @Size(max = 500, message = "备注不能超过500个字符")
    @ApiModelProperty("备注")
    private String remark;

    @Valid
    @Size(min = 1, message = "预约至少需要一个服务项目")
    @ApiModelProperty("预约项目明细")
    private List<AppointmentItemDTO> items;
}
