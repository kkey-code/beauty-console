package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 客户档案请求参数，承载新增或修改接口的数据并声明输入校验规则。
 */
@Data
@ApiModel(value = "CustomerProfileDTO", description = "客户档案保存参数")
public class CustomerProfileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键，新增时为空")
    private Long id;

    @NotBlank(message = "客户姓名不能为空")
    @Size(max = 50, message = "客户姓名不能超过50个字符")
    @ApiModelProperty("客户姓名")
    private String name;

    @Size(max = 20, message = "手机号不能超过20个字符")
    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("性别：1男，2女")
    private Integer gender;

    @ApiModelProperty("生日")
    private LocalDate birthday;

    @ApiModelProperty("客户等级：0普通，1银卡，2金卡，3VIP")
    private Integer level;

    @Size(max = 50, message = "客户来源不能超过50个字符")
    @ApiModelProperty("客户来源")
    private String source;

    @Size(max = 500, message = "备注不能超过500个字符")
    @ApiModelProperty("备注")
    private String remark;
}
