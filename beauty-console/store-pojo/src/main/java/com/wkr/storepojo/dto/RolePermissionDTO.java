package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value = "RolePermissionDTO", description = "角色默认权限保存参数")
public class RolePermissionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "权限点不能为空")
    @ApiModelProperty("角色默认权限编码列表")
    private List<String> permissionCodes;
}
