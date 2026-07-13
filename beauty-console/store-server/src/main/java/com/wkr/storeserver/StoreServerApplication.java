package com.wkr.storeserver;

import com.wkr.storecommon.properties.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 后台管理系统启动入口，负责创建并运行 Spring Boot 应用上下文。
 */
@MapperScan("com.wkr.storeserver.mapper")
@EnableCaching
@EnableConfigurationProperties(JwtProperties.class)
@SpringBootApplication
public class StoreServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoreServerApplication.class, args);
    }
}
