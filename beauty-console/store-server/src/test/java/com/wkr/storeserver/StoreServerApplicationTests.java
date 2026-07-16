package com.wkr.storeserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring Boot 上下文测试，验证应用核心配置和 Bean 能够正常加载。
 */
@SpringBootTest(properties = "store.database.compatibility-initializer-enabled=false")
class StoreServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
