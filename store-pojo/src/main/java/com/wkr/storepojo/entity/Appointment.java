package com.wkr.storepojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预约实体，映射数据库记录并供 MyBatis-Plus 完成持久化操作。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("appointment")
@ApiModel(value = "Appointment对象", description = "预约主表")
public class Appointment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("预约编号")
    private String appointmentNo;

    @ApiModelProperty("客户ID")
    private Long customerId;

    @ApiModelProperty("主服务员工ID")
    private Long staffId;

    @ApiModelProperty("预约时间")
    private LocalDateTime appointmentTime;

    @ApiModelProperty("状态：0待确认，1已确认，2已完成，3已取消")
    private Integer status;

    @ApiModelProperty("预计总时长，单位分钟")
    private Integer totalDurationMinutes;

    @ApiModelProperty("备注")
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @ApiModelProperty("逻辑删除：0未删除，1已删除")
    private Integer deleted;
}
