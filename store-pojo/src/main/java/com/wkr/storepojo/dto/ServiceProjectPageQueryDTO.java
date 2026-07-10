package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务项目分页查询参数，封装页码以及该模块支持的筛选条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "ServiceProjectPageQueryDTO", description = "服务项目分页查询参数")
public class ServiceProjectPageQueryDTO extends PageQueryDTO {

    @ApiModelProperty("项目名称")
    private String name;

    @ApiModelProperty("项目分类")
    private String category;

    @ApiModelProperty("状态：0下架，1上架")
    private Integer status;
}
