package com.wkr.storepojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户实体，映射数据库记录并供 MyBatis-Plus 完成持久化操作。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user")
@ApiModel(value = "SysUser对象", description = "用户账号表")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("登录账号")
    private String username;

    @ApiModelProperty("登录密码/密码哈希")
    private String passwordHash;

    @ApiModelProperty("角色ID：1超级管理员，2店长，3普通员工，4库存管理员，5财务/收银，6只读")
    private Integer roleId;

    @ApiModelProperty("关联员工ID")
    private Long staffId;

    @ApiModelProperty("状态：0禁用，1启用")
    private Integer status;

    @ApiModelProperty("最后登录时间")
    private LocalDateTime lastLoginTime;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
