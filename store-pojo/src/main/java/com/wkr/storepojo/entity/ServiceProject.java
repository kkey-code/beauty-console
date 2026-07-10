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
 * 服务项目实体，映射数据库记录并供 MyBatis-Plus 完成持久化操作。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("service_project")
@ApiModel(value = "ServiceProject对象", description = "服务项目表")
public class ServiceProject implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
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

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
