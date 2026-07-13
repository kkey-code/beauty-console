package com.wkr.storepojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务项目响应对象，组合接口返回给前端展示的业务字段。
 */
@Data
@ApiModel(value = "ServiceProjectVO", description = "服务项目展示对象")
public class ServiceProjectVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("项目名称")
    private String name;

    @ApiModelProperty("项目分类")
    private String category;

    @ApiModelProperty("标准价格")
    private BigDecimal price;

    @ApiModelProperty("服务时长，单位分钟")
    private Integer durationMinutes;

    @ApiModelProperty("项目说明")
    private String description;

    @ApiModelProperty("状态：0下架，1上架")
    private Integer status;

    @ApiModelProperty("状态名称")
    private String statusName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
