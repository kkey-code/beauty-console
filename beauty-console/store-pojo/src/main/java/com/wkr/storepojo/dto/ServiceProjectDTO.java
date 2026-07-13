package com.wkr.storepojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 服务项目请求参数，承载新增或修改接口的数据并声明输入校验规则。
 */
@Data
@ApiModel(value = "ServiceProjectDTO", description = "服务项目保存参数")
public class ServiceProjectDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键，新增时为空")
    private Long id;

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称不能超过100个字符")
    @ApiModelProperty("项目名称")
    private String name;

    @Size(max = 50, message = "项目分类不能超过50个字符")
    @ApiModelProperty("项目分类")
    private String category;

    @NotNull(message = "标准价格不能为空")
    @DecimalMin(value = "0.00", message = "标准价格不能小于0")
    @ApiModelProperty("标准价格")
    private BigDecimal price;

    @Min(value = 0, message = "服务时长不能小于0")
    @ApiModelProperty("服务时长，单位分钟")
    private Integer durationMinutes;

    @Size(max = 500, message = "项目说明不能超过500个字符")
    @ApiModelProperty("项目说明")
    private String description;

    @NotNull(message = "状态不能为空")
    @ApiModelProperty("状态：0下架，1上架")
    private Integer status;
}
