package com.wkr.storepojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
 * 服务订单实体，映射数据库记录并供 MyBatis-Plus 完成持久化操作。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("service_order")
@ApiModel(value = "ServiceOrder对象", description = "订单主表")
public class ServiceOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("关联预约ID，可为空")
    private Long appointmentId;

    @ApiModelProperty("客户ID")
    private Long customerId;

    @ApiModelProperty("订单类型：service/time_card/care_card/member_card/course_card")
    private String orderType;

    @ApiModelProperty("原价金额")
    private BigDecimal originalAmount;

    @ApiModelProperty("优惠金额")
    private BigDecimal discountAmount;

    @ApiModelProperty("应收金额")
    private BigDecimal receivableAmount;

    @ApiModelProperty("已收金额")
    private BigDecimal paidAmount;

    @ApiModelProperty("欠款金额")
    private BigDecimal debtAmount;

    @ApiModelProperty("欠款状态：0无欠款，1分期中，2已结清")
    private Integer debtStatus;

    @ApiModelProperty("支付状态：0未支付，1部分支付，2已支付，3已退款")
    private Integer payStatus;

    @ApiModelProperty("订单状态：0待服务，1已完成，2已取消")
    private Integer orderStatus;

    @ApiModelProperty("备注")
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @ApiModelProperty("逻辑删除：0未删除，1已删除")
    private Integer deleted;
}
