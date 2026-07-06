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
@TableName("payment_record")
@ApiModel(value = "PaymentRecord对象", description = "收款流水表")
public class PaymentRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("收款流水号")
    private String paymentNo;

    @ApiModelProperty("支付方式：wechat/alipay/cash/time_card")
    private String paymentMethod;

    @ApiModelProperty("收款金额")
    private BigDecimal payAmount;

    @ApiModelProperty("状态：0未确认，1成功，2退款，3作废")
    private Integer payStatus;

    @ApiModelProperty("收款时间")
    private LocalDateTime payTime;

    @ApiModelProperty("操作人ID")
    private Long operatorId;

    @ApiModelProperty("备注")
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
