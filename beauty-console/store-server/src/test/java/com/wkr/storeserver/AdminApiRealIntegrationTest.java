package com.wkr.storeserver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnabledIfSystemProperty(named = "store.real-it", matches = "true")
@SpringBootTest(properties = {
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.datasource.url=${STORE_REAL_DB_URL:jdbc:mysql://localhost:3306/db_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true}",
        "spring.datasource.username=${STORE_REAL_DB_USERNAME:root}",
        "spring.datasource.password=${STORE_REAL_DB_PASSWORD:12345678}",
        "spring.cache.type=redis",
        "spring.data.redis.host=${STORE_REAL_REDIS_HOST:localhost}",
        "spring.data.redis.port=${STORE_REAL_REDIS_PORT:6379}",
        "spring.data.redis.password=${STORE_REAL_REDIS_PASSWORD:}",
        "spring.data.redis.database=${STORE_REAL_REDIS_DATABASE:0}"
})
@AutoConfigureMockMvc
class AdminApiRealIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    void adminCanUseRealMysqlAndRedis() throws Exception {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            assertNotNull(connection.ping());
        }

        String token = loginAndGetToken();

        mockMvc.perform(get("/admin/inventory-skus")
                        .header("token", token)
                        .param("page", "1")
                        .param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/admin/service-project-inventories")
                        .header("token", token)
                        .param("page", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private String loginAndGetToken() throws Exception {
        String response = mockMvc.perform(post("/admin/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("token").asText();
    }
}
