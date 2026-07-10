package com.wkr.storepojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务项目耗材关系实体，维护服务项目完成时需要扣减的库存 SKU 和默认消耗数量。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("service_project_inventory")
@ApiModel(value = "ServiceProjectInventory对象", description = "服务项目耗材关系表")
public class ServiceProjectInventory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("服务项目ID")
    private Long serviceProjectId;

    @ApiModelProperty("库存物品ID")
    private Long inventoryId;

    @ApiModelProperty("单次服务默认消耗数量")
    private BigDecimal consumeQuantity;

    @ApiModelProperty("状态：0停用，1启用")
    private Integer status;

    @ApiModelProperty("备注")
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
