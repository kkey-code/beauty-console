package com.wkr.storeserver;

import com.wkr.storecommon.properties.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@MapperScan("com.wkr.storeserver.mapper")
@EnableConfigurationProperties(JwtProperties.class)
@SpringBootApplication
public class StoreServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoreServerApplication.class, args);
    }
}
