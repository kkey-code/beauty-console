package com.wkr.storepojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录用户响应对象，组合接口返回给前端展示的业务字段。
 */
@Data
@ApiModel(value = "LoginUserVO", description = "登录结果")
public class LoginUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户ID")
    private Long id;

    @ApiModelProperty("登录账号")
    private String username;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("角色ID：1超级管理员，2店长，3普通员工，4库存管理员，5财务/收银，6只读")
    private Integer roleId;

    @ApiModelProperty("角色名称")
    private String roleName;

    @ApiModelProperty("员工ID")
    private Long staffId;

    @ApiModelProperty("状态")
    private Integer status; // 0:禁用 1:启用

    @ApiModelProperty("员工姓名")
    private String staffName;

    @ApiModelProperty("访问令牌")
    private String token;

    @ApiModelProperty("角色编码")
    private String roleCode;
}
