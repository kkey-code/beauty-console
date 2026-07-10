package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 后台用户登录参数，承载账号和明文密码并执行非空校验。
 */
@Data
@ApiModel(value = "SysUserLoginDTO", description = "登录参数")
public class SysUserLoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "账号不能为空")
    @ApiModelProperty("登录账号")
    private String username;

    @NotBlank(message = "密码不能为空")
    @ApiModelProperty("登录密码")
    private String password;
}
