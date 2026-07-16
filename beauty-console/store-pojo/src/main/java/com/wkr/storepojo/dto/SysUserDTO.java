package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统用户请求参数，承载新增或修改接口的数据并声明输入校验规则。
 */
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
    @Min(value = 1, message = "角色ID必须在1到6之间")
    @Max(value = 6, message = "角色ID必须在1到6之间")
    @ApiModelProperty("角色ID：1超级管理员，2店长，3普通员工，4库存管理员，5财务/收银，6只读")
    private Integer roleId;

    @NotNull(message = "关联员工不能为空")
    @ApiModelProperty("关联员工ID；一个员工只能关联一个账号")
    private Long staffId;

    @NotNull(message = "状态不能为空")
    @ApiModelProperty("状态：0禁用，1启用")
    private Integer status;
}
