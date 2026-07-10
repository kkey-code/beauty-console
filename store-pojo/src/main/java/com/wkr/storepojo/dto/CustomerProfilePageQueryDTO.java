package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户档案分页查询参数，封装页码以及该模块支持的筛选条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "CustomerProfilePageQueryDTO", description = "客户档案分页查询参数")
public class CustomerProfilePageQueryDTO extends PageQueryDTO {

    @ApiModelProperty("客户姓名")
    private String name;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("客户等级：0普通，1银卡，2金卡，3VIP")
    private Integer level;

    @ApiModelProperty("客户来源")
    private String source;
}
