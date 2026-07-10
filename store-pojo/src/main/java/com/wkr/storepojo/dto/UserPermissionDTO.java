package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户权限保存参数。为空列表时清空用户级配置并回到角色默认权限。
 */
@Data
@ApiModel(value = "UserPermissionDTO", description = "用户权限保存参数")
public class UserPermissionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "权限点不能为空")
    @ApiModelProperty("权限编码列表")
    private List<String> permissionCodes;
}
