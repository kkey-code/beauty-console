package com.wkr.storeserver.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * 门店后台 OpenAPI 元数据和 JWT 请求头说明。
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "门店后台管理系统接口文档",
                version = "1.0.0",
                description = "账号权限、员工、客户、服务项目、预约、订单、收款和库存接口",
                contact = @Contact(name = "beauty-console")
        )
)
@SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "token",
        description = "登录后填写 Bearer + 空格 + JWT；登录接口无需填写"
)
public class OpenApiConfiguration {
}
