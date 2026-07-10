package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户权限保存参数。useRoleDefault 为 true 时回到角色默认权限；空列表表示用户级权限清空。
 */
@Data
@ApiModel(value = "UserPermissionDTO", description = "用户权限保存参数")
public class UserPermissionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "权限点不能为空")
    @ApiModelProperty("权限编码列表")
    private List<String> permissionCodes;

    @ApiModelProperty("是否恢复为当前角色默认权限")
    private Boolean useRoleDefault;
}
