package com.wkr.storepojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Operation audit log for high-risk management actions.
 */
@Data
@TableName("operation_audit_log")
@ApiModel(value = "OperationAuditLog对象", description = "操作审计日志表")
public class OperationAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("操作账号ID")
    private Long operatorUserId;

    @ApiModelProperty("操作员工ID")
    private Long operatorStaffId;

    @ApiModelProperty("操作类型")
    private String actionType;

    @ApiModelProperty("对象类型")
    private String targetType;

    @ApiModelProperty("对象ID")
    private Long targetId;

    @ApiModelProperty("操作说明")
    private String detail;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
