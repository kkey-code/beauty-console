package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 预约项目明细请求参数，承载新增或修改接口的数据并声明输入校验规则。
 */
@Data
@ApiModel(value = "AppointmentItemDTO", description = "预约项目明细参数")
public class AppointmentItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键，新增时为空")
    private Long id;

    @ApiModelProperty("预约ID")
    private Long appointmentId;

    @NotNull(message = "服务项目ID不能为空")
    @ApiModelProperty("服务项目ID")
    private Long serviceProjectId;

    @ApiModelProperty("指定服务员工ID")
    private Long staffId;

    @Size(max = 100, message = "项目名称不能超过100个字符")
    @ApiModelProperty("项目快照名称")
    private String serviceName;

    @DecimalMin(value = "0.00", message = "项目价格不能小于0")
    @ApiModelProperty("项目快照价格")
    private BigDecimal price;

    @Min(value = 0, message = "项目时长不能小于0")
    @ApiModelProperty("项目快照时长，单位分钟")
    private Integer durationMinutes;

    @ApiModelProperty("排序")
    private Integer sortNo;
}
