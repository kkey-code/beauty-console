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
@TableName("inventory_stock_log")
@ApiModel(value = "InventoryStockLog对象", description = "库存流水表")
public class InventoryStockLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("库存物品ID")
    private Long inventoryId;

    @ApiModelProperty("类型：stock_in/stock_out/check/loss/return")
    private String changeType;

    @ApiModelProperty("变动数量")
    private BigDecimal changeQuantity;

    @ApiModelProperty("变动前库存")
    private BigDecimal beforeQuantity;

    @ApiModelProperty("变动后库存")
    private BigDecimal afterQuantity;

    @ApiModelProperty("关联订单ID，可为空")
    private Long relatedOrderId;

    @ApiModelProperty("操作人ID")
    private Long operatorId;

    @ApiModelProperty("备注")
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
