package com.wkr.storepojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户档案响应对象，组合接口返回给前端展示的业务字段。
 */
@Data
@ApiModel(value = "CustomerProfileVO", description = "客户档案展示对象")
public class CustomerProfileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("客户姓名")
    private String name;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("性别：1男，2女")
    private Integer gender;

    @ApiModelProperty("性别名称")
    private String genderName;

    @ApiModelProperty("生日")
    private LocalDate birthday;

    @ApiModelProperty("客户等级")
    private Integer level;

    @ApiModelProperty("客户等级名称")
    private String levelName;

    @ApiModelProperty("客户来源")
    private String source;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
