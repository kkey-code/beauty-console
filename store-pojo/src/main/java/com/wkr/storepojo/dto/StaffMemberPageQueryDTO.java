package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "StaffMemberPageQueryDTO", description = "员工分页查询参数")
public class StaffMemberPageQueryDTO extends PageQueryDTO {

    @ApiModelProperty("员工姓名")
    private String name;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("状态：0停用，1启用")
    private Integer status;
}
