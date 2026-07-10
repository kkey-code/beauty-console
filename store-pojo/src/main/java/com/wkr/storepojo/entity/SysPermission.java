package com.wkr.storepojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限点实体，描述菜单、按钮和接口访问规则。
 */
@Data
@TableName("sys_permission")
@ApiModel(value = "SysPermission对象", description = "权限点表")
public class SysPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("权限编码")
    private String permissionCode;

    @ApiModelProperty("权限名称")
    private String permissionName;

    @ApiModelProperty("权限分组")
    private String permissionGroup;

    @ApiModelProperty("HTTP 方法，菜单权限可为空")
    private String method;

    @ApiModelProperty("接口路径匹配规则，菜单权限可为空")
    private String pathPattern;

    @ApiModelProperty("前端菜单路径")
    private String menuPath;

    @ApiModelProperty("前端资源操作标识")
    private String actionKey;

    @ApiModelProperty("状态：0停用，1启用")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
