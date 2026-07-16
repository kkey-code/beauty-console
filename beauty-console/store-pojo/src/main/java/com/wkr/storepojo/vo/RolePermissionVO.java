package com.wkr.storepojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value = "RolePermissionVO", description = "角色默认权限展示对象")
public class RolePermissionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("角色ID")
    private Integer roleId;

    @ApiModelProperty("角色编码")
    private String roleCode;

    @ApiModelProperty("角色名称")
    private String roleName;

    @ApiModelProperty("当前角色默认权限编码")
    private List<String> permissionCodes;

    @ApiModelProperty("该角色可选权限点")
    private List<PermissionPointVO> allPermissions;
}
