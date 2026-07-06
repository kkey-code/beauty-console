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

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("inventory_sku")
@ApiModel(value = "InventorySku对象", description = "库存物品表")
public class InventorySku implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("库存名称")
    private String name;

    @ApiModelProperty("分类")
    private String category;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("当前库存")
    private BigDecimal quantity;

    @ApiModelProperty("安全库存")
    private BigDecimal safetyStock;

    @ApiModelProperty("成本价")
    private BigDecimal costPrice;

    @ApiModelProperty("供应商")
    private String supplier;

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
