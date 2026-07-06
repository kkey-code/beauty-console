package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "StaffMemberDTO", description = "员工保存参数")
public class StaffMemberDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键，新增时为空")
    private Long id;

    @NotBlank(message = "员工姓名不能为空")
    @Size(max = 50, message = "员工姓名不能超过50个字符")
    @ApiModelProperty("员工姓名")
    private String name;

    @Size(max = 20, message = "手机号不能超过20个字符")
    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("性别：1男，2女")
    private Integer gender;

    @Size(max = 50, message = "职位不能超过50个字符")
    @ApiModelProperty("职位")
    private String position;

    @NotNull(message = "状态不能为空")
    @ApiModelProperty("状态：0停用，1启用")
    private Integer status;

    @Size(max = 500, message = "备注不能超过500个字符")
    @ApiModelProperty("备注")
    private String remark;
}
