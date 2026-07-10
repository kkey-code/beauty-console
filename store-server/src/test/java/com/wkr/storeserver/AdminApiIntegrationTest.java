package com.wkr.storeserver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkr.storepojo.entity.InventorySku;
import com.wkr.storeserver.mapper.InventorySkuMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:store_it;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.cache.type=none"
})
@AutoConfigureMockMvc
@Sql(scripts = {"/integration/schema.sql", "/integration/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AdminApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventorySkuMapper inventorySkuMapper;

    @Test
    void adminLoginPaginationAndOutboundInventoryUseRealDatabase() throws Exception {
        String token = loginAndGetToken("admin");

        mockMvc.perform(get("/admin/inventory-skus")
                        .header("token", token)
                        .param("page", "1")
                        .param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].name").value("Bubble Cleaner"));

        mockMvc.perform(post("/admin/inventory-stock-logs/outbound")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"inventoryId":1001,"changeType":"stock_out","changeQuantity":2,"relatedOrderId":9001,"remark":"integration"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        InventorySku inventorySku = inventorySkuMapper.selectById(1001L);
        assertEquals(0, new BigDecimal("3.00").compareTo(inventorySku.getQuantity()));

        mockMvc.perform(get("/admin/permissions").header("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].permissionCode").exists());
    }

    @Test
    void inventoryAccountCannotVisitAccountManagementApis() throws Exception {
        String token = loginAndGetToken("inventory");

        mockMvc.perform(get("/admin/users")
                        .header("token", token)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isForbidden());
    }

    private String loginAndGetToken(String username) throws Exception {
        String response = mockMvc.perform(post("/admin/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"123456"}
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("token").asText();
    }
}
