package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户状态修改参数，承载账号启用或禁用状态。
 */
@Data
@ApiModel(value = "SysUserStatusDTO", description = "用户状态修改参数")
public class SysUserStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "状态不能为空")
    @ApiModelProperty("状态：0禁用，1启用")
    private Integer status;
}
