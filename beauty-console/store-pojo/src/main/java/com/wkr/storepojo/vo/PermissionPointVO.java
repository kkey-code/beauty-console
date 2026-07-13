package com.wkr.storepojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 权限点展示对象。
 */
@Data
@ApiModel(value = "PermissionPointVO", description = "权限点展示对象")
public class PermissionPointVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("权限ID")
    private Long id;

    @ApiModelProperty("权限编码")
    private String permissionCode;

    @ApiModelProperty("权限名称")
    private String permissionName;

    @ApiModelProperty("权限分组")
    private String permissionGroup;

    @ApiModelProperty("菜单路径")
    private String menuPath;

    @ApiModelProperty("操作标识")
    private String actionKey;
}
