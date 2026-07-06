package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "SysUserPageQueryDTO", description = "用户账号分页查询参数")
public class SysUserPageQueryDTO extends PageQueryDTO {

    @ApiModelProperty("登录账号")
    private String username;

    @ApiModelProperty("角色编码")
    private Integer roleId;

    @ApiModelProperty("状态：0禁用，1启用")
    private Integer status;
}
