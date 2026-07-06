package com.wkr.storepojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("appointment_item")
@ApiModel(value = "AppointmentItem对象", description = "预约项目明细表")
public class AppointmentItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("预约ID")
    private Long appointmentId;

    @ApiModelProperty("服务项目ID")
    private Long serviceProjectId;

    @ApiModelProperty("指定服务员工ID")
    private Long staffId;

    @ApiModelProperty("项目快照名称")
    private String serviceName;

    @ApiModelProperty("项目快照价格")
    private BigDecimal price;

    @ApiModelProperty("项目快照时长，单位分钟")
    private Integer durationMinutes;

    @ApiModelProperty("排序")
    private Integer sortNo;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
