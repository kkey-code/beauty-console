package com.wkr.storecommon.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置属性，绑定密钥、有效期和请求头名称等管理端令牌配置。
 */
@Data
@ConfigurationProperties(prefix = "sky.jwt")
public class JwtProperties {

    /**
     * 管理端员工生成jwt令牌相关配置
     */
    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;

}
