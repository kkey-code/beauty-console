package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "SysUserDTO", description = "用户账号保存参数")
public class SysUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键，新增时为空")
    private Long id;

    @NotBlank(message = "登录账号不能为空")
    @Size(max = 50, message = "登录账号不能超过50个字符")
    @ApiModelProperty("登录账号")
    private String username;

    @Size(max = 100, message = "密码不能超过100个字符")
    @ApiModelProperty("登录密码/密码哈希")
    private String passwordHash;

    @NotNull(message = "角色不能为空")
    @ApiModelProperty("角色ID：0只读，1管理员，2员工")
    private Integer roleId;

    @ApiModelProperty("关联员工ID")
    private Long staffId;

    @NotNull(message = "状态不能为空")
    @ApiModelProperty("状态：0禁用，1启用")
    private Integer status;
}
