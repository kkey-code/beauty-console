package com.wkr.storepojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户权限配置展示对象。
 */
@Data
@ApiModel(value = "UserPermissionVO", description = "用户权限配置展示对象")
public class UserPermissionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("是否存在用户级覆盖配置")
    private Boolean customized;

    @ApiModelProperty("当前生效权限编码")
    private List<String> permissionCodes;

    @ApiModelProperty("当前角色默认权限编码")
    private List<String> rolePermissionCodes;

    @ApiModelProperty("全部可选权限点")
    private List<PermissionPointVO> allPermissions;
}
