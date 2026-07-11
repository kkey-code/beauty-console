package com.wkr.storeserver;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkr.storepojo.entity.Appointment;
import com.wkr.storepojo.entity.InventorySku;
import com.wkr.storepojo.entity.OperationAuditLog;
import com.wkr.storepojo.entity.PaymentRecord;
import com.wkr.storepojo.entity.ServiceOrder;
import com.wkr.storeserver.mapper.AppointmentMapper;
import com.wkr.storeserver.mapper.InventorySkuMapper;
import com.wkr.storeserver.mapper.OperationAuditLogMapper;
import com.wkr.storeserver.mapper.PaymentRecordMapper;
import com.wkr.storeserver.mapper.ServiceOrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Autowired
    private ServiceOrderMapper serviceOrderMapper;

    @Autowired
    private PaymentRecordMapper paymentRecordMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private OperationAuditLogMapper operationAuditLogMapper;

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

    @Test
    void serviceProjectInventoryCrudFlowUsesRealDatabase() throws Exception {
        String token = loginAndGetToken("admin");

        String createResponse = mockMvc.perform(post("/admin/service-project-inventories")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"serviceProjectId":1001,"inventoryId":1001,"consumeQuantity":1.50,"status":1,"remark":"integration"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long relationId = objectMapper.readTree(createResponse).path("data").asLong();

        mockMvc.perform(get("/admin/service-project-inventories")
                        .header("token", token)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].serviceProjectName").value("Basic Facial"))
                .andExpect(jsonPath("$.data.records[0].inventoryName").value("Bubble Cleaner"))
                .andExpect(jsonPath("$.data.records[0].inventoryUnit").value("bottle"));

        mockMvc.perform(get("/admin/service-project-inventories/{id}", relationId)
                        .header("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.consumeQuantity").value(1.5));

        mockMvc.perform(put("/admin/service-project-inventories/{id}", relationId)
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"serviceProjectId":1001,"inventoryId":1001,"consumeQuantity":2.00,"status":1,"remark":"updated"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/admin/service-project-inventories/{id}", relationId)
                        .header("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.consumeQuantity").value(2.0))
                .andExpect(jsonPath("$.data.remark").value("updated"));

        mockMvc.perform(delete("/admin/service-project-inventories/{id}", relationId)
                        .header("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/admin/service-project-inventories")
                        .header("token", token)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void userPermissionsCanBeClearedOrRestoredToRoleDefault() throws Exception {
        String token = loginAndGetToken("admin");

        mockMvc.perform(put("/admin/users/2/permissions")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"permissionCodes":["dashboard:view"],"useRoleDefault":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/admin/users/2/permissions")
                        .header("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customized").value(true))
                .andExpect(jsonPath("$.data.permissionCodes.length()").value(1))
                .andExpect(jsonPath("$.data.rolePermissionCodes.length()").value(3));

        mockMvc.perform(put("/admin/users/2/permissions")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"permissionCodes":[],"useRoleDefault":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/admin/users/2/permissions")
                        .header("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customized").value(true))
                .andExpect(jsonPath("$.data.permissionCodes.length()").value(0))
                .andExpect(jsonPath("$.data.rolePermissionCodes.length()").value(3));

        mockMvc.perform(put("/admin/users/2/permissions")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"permissionCodes":[],"useRoleDefault":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/admin/users/2/permissions")
                        .header("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customized").value(false))
                .andExpect(jsonPath("$.data.permissionCodes.length()").value(3))
                .andExpect(jsonPath("$.data.rolePermissionCodes.length()").value(3));
    }

    @Test
    void paymentCreateAndVoidUpdateOrderAndWriteAudit() throws Exception {
        String token = loginAndGetToken("admin");

        String createResponse = mockMvc.perform(post("/admin/payment-records")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":3001,"paymentMethod":"wechat","payAmount":60.00,"payStatus":1,"remark":"integration"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long paymentId = objectMapper.readTree(createResponse).path("data").asLong();

        ServiceOrder paidOrder = serviceOrderMapper.selectById(3001L);
        assertEquals(0, new BigDecimal("60.00").compareTo(paidOrder.getPaidAmount()));
        assertEquals(0, new BigDecimal("40.00").compareTo(paidOrder.getDebtAmount()));
        assertEquals(1, paidOrder.getPayStatus());

        PaymentRecord record = paymentRecordMapper.selectById(paymentId);
        assertTrue(record.getPaymentNo().startsWith("PAY"));

        mockMvc.perform(patch("/admin/payment-records/{id}/void", paymentId)
                        .header("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        ServiceOrder voidedOrder = serviceOrderMapper.selectById(3001L);
        assertEquals(0, BigDecimal.ZERO.compareTo(voidedOrder.getPaidAmount()));
        assertEquals(0, new BigDecimal("100.00").compareTo(voidedOrder.getDebtAmount()));
        assertEquals(0, voidedOrder.getPayStatus());

        Long auditCount = operationAuditLogMapper.selectCount(new LambdaQueryWrapper<OperationAuditLog>()
                .eq(OperationAuditLog::getTargetType, "PAYMENT_RECORD"));
        assertTrue(auditCount >= 2);
    }

    @Test
    void paymentRejectsOverpayAfterLatestOrderAmountIsRecalculated() throws Exception {
        String token = loginAndGetToken("admin");

        mockMvc.perform(post("/admin/payment-records")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":3001,"paymentMethod":"cash","payAmount":90.00,"payStatus":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(post("/admin/payment-records")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":3001,"paymentMethod":"cash","payAmount":20.00,"payStatus":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        ServiceOrder order = serviceOrderMapper.selectById(3001L);
        assertEquals(0, new BigDecimal("90.00").compareTo(order.getPaidAmount()));
        assertEquals(1, paymentRecordMapper.selectCount(new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getOrderId, 3001L)));
    }

    @Test
    void appointmentStatusTransitionRejectsInvalidRepeatConfirm() throws Exception {
        String token = loginAndGetToken("admin");

        mockMvc.perform(patch("/admin/appointments/{id}/confirm", 4001L)
                        .header("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Appointment confirmed = appointmentMapper.selectById(4001L);
        assertEquals(1, confirmed.getStatus());

        mockMvc.perform(patch("/admin/appointments/{id}/confirm", 4001L)
                        .header("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void appointmentWithGeneratedOrderCannotBeDeleted() throws Exception {
        String token = loginAndGetToken("admin");

        mockMvc.perform(delete("/admin/appointments/{id}", 4002L)
                        .header("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        Appointment appointment = appointmentMapper.selectById(4002L);
        assertEquals(0, appointment.getDeleted());
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
