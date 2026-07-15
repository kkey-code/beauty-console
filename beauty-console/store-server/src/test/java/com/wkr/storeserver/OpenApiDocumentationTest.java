package com.wkr.storeserver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:openapi_test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.cache.type=none"
})
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

    private static final Set<String> HTTP_METHODS = Set.of(
            "get", "post", "put", "patch", "delete", "head", "options", "trace"
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void knife4jPageIsAvailable() throws Exception {
        mockMvc.perform(get("/doc.html"))
                .andExpect(status().isOk());
    }

    @Test
    void adminOpenApiContainsAllBusinessOperations() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs/admin"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode document = objectMapper.readTree(body);
        assertThat(document.path("info").path("title").asText())
                .isEqualTo("门店后台管理系统接口文档");
        assertThat(document.path("paths").path("/admin/service-orders").path("post")
                .path("summary").asText()).isEqualTo("添加订单");
        assertThat(document.path("paths").path("/admin/service-orders/idempotency-token").path("post")
                .path("summary").asText()).isEqualTo("获取下单幂等令牌");
        assertThat(document.path("paths").path("/admin/dashboard/overview").path("get")
                .path("summary").asText()).isEqualTo("获取工作台经营概览");
        assertThat(countOperations(document.path("paths"))).isEqualTo(75);
    }

    private int countOperations(JsonNode paths) {
        int count = 0;
        for (JsonNode pathItem : paths) {
            for (String method : HTTP_METHODS) {
                if (pathItem.has(method)) {
                    count++;
                }
            }
        }
        return count;
    }
}
