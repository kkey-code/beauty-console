package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户分页查询参数，封装页码以及该模块支持的筛选条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "SysUserPageQueryDTO", description = "用户账号分页查询参数")
public class SysUserPageQueryDTO extends PageQueryDTO {

    @ApiModelProperty("登录账号")
    private String username;

    @ApiModelProperty("角色ID")
    private Integer roleId;

    @ApiModelProperty("状态：0禁用，1启用")
    private Integer status;
}
