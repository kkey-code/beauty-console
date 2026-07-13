package com.wkr.storepojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务项目耗材关系响应对象，组合项目名称、库存名称和默认消耗数量供前端展示。
 */
@Data
@ApiModel(value = "ServiceProjectInventoryVO", description = "服务项目耗材关系展示对象")
public class ServiceProjectInventoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("服务项目ID")
    private Long serviceProjectId;

    @ApiModelProperty("服务项目名称")
    private String serviceProjectName;

    @ApiModelProperty("库存物品ID")
    private Long inventoryId;

    @ApiModelProperty("库存物品名称")
    private String inventoryName;

    @ApiModelProperty("库存单位")
    private String inventoryUnit;

    @ApiModelProperty("单次服务默认消耗数量")
    private BigDecimal consumeQuantity;

    @ApiModelProperty("状态：0停用，1启用")
    private Integer status;

    @ApiModelProperty("状态名称")
    private String statusName;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
