package com.wkr.storeserver;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storepojo.dto.AppointmentPageQueryDTO;
import com.wkr.storepojo.dto.ServiceOrderDTO;
import com.wkr.storepojo.dto.ServiceOrderPageQueryDTO;
import com.wkr.storepojo.dto.SysUserLoginDTO;
import com.wkr.storepojo.dto.SysUserPageQueryDTO;
import com.wkr.storepojo.entity.Appointment;
import com.wkr.storepojo.entity.AppointmentItem;
import com.wkr.storepojo.entity.CustomerProfile;
import com.wkr.storepojo.entity.InventorySku;
import com.wkr.storepojo.entity.InventoryStockLog;
import com.wkr.storepojo.entity.PaymentRecord;
import com.wkr.storepojo.entity.ServiceOrder;
import com.wkr.storepojo.entity.ServiceOrderItem;
import com.wkr.storepojo.entity.ServiceProject;
import com.wkr.storepojo.entity.ServiceProjectInventory;
import com.wkr.storepojo.entity.StaffMember;
import com.wkr.storepojo.entity.SysUser;
import com.wkr.storepojo.vo.AppointmentVO;
import com.wkr.storepojo.vo.LoginUserVO;
import com.wkr.storepojo.vo.PaymentRecordVO;
import com.wkr.storepojo.vo.PermissionPointVO;
import com.wkr.storepojo.vo.ServiceOrderItemVO;
import com.wkr.storepojo.vo.ServiceOrderVO;
import com.wkr.storepojo.vo.SysUserVO;
import com.wkr.storepojo.vo.UserPermissionVO;
import com.wkr.storeserver.controller.AppointmentController;
import com.wkr.storeserver.controller.AppointmentItemController;
import com.wkr.storeserver.controller.CustomerProfileController;
import com.wkr.storeserver.controller.InventorySkuController;
import com.wkr.storeserver.controller.InventoryStockLogController;
import com.wkr.storeserver.controller.PaymentRecordController;
import com.wkr.storeserver.controller.PermissionController;
import com.wkr.storeserver.controller.ServiceOrderController;
import com.wkr.storeserver.controller.ServiceOrderItemController;
import com.wkr.storeserver.controller.ServiceProjectController;
import com.wkr.storeserver.controller.ServiceProjectInventoryController;
import com.wkr.storeserver.controller.StaffMemberController;
import com.wkr.storeserver.controller.SysUserController;
import com.wkr.storeserver.handler.GlobalExceptionHandler;
import com.wkr.storeserver.service.AppointmentItemService;
import com.wkr.storeserver.service.AppointmentService;
import com.wkr.storeserver.service.CustomerProfileService;
import com.wkr.storeserver.service.DeletionGuardService;
import com.wkr.storeserver.service.InventorySkuService;
import com.wkr.storeserver.service.InventoryStockLogService;
import com.wkr.storeserver.service.PaymentRecordService;
import com.wkr.storeserver.service.PermissionPointService;
import com.wkr.storeserver.service.ServiceOrderItemService;
import com.wkr.storeserver.service.ServiceOrderService;
import com.wkr.storeserver.service.ServiceProjectService;
import com.wkr.storeserver.service.ServiceProjectInventoryService;
import com.wkr.storeserver.service.StaffMemberService;
import com.wkr.storeserver.service.SysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller 接口单元测试，使用 MockMvc 和模拟服务验证全部管理端接口的请求绑定与统一响应。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ControllerApiUnitTest {

    private MockMvc mockMvc;

    @Mock
    private SysUserService sysUserService;
    @Mock
    private PermissionPointService permissionPointService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private StaffMemberService staffMemberService;
    @Mock
    private ServiceProjectService serviceProjectService;
    @Mock
    private ServiceProjectInventoryService serviceProjectInventoryService;
    @Mock
    private ServiceOrderItemService serviceOrderItemService;
    @Mock
    private ServiceOrderService serviceOrderService;
    @Mock
    private PaymentRecordService paymentRecordService;
    @Mock
    private InventoryStockLogService inventoryStockLogService;
    @Mock
    private InventorySkuService inventorySkuService;
    @Mock
    private CustomerProfileService customerProfileService;
    @Mock
    private AppointmentItemService appointmentItemService;
    @Mock
    private AppointmentService appointmentService;
    @Mock
    private DeletionGuardService deletionGuardService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new SysUserController(sysUserService, passwordEncoder, permissionPointService),
                        new PermissionController(permissionPointService),
                        new StaffMemberController(staffMemberService, deletionGuardService),
                        new ServiceProjectController(serviceProjectService, deletionGuardService),
                        new ServiceProjectInventoryController(
                                serviceProjectInventoryService,
                                serviceProjectService,
                                inventorySkuService),
                        new ServiceOrderItemController(serviceOrderItemService, staffMemberService),
                        new ServiceOrderController(serviceOrderService),
                        new PaymentRecordController(paymentRecordService),
                        new InventoryStockLogController(inventoryStockLogService, inventorySkuService),
                        new InventorySkuController(inventorySkuService, deletionGuardService),
                        new CustomerProfileController(customerProfileService, deletionGuardService),
                        new AppointmentItemController(appointmentItemService, serviceProjectService),
                        new AppointmentController(appointmentService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        stubCommonServiceMethods();
    }

    @Test
    void sysUserEndpointsReturnSuccess() throws Exception {
        mockMvc.perform(post("/admin/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"123456"}
                                """))
                .andExpect(okWithCodeOne());

        mockMvc.perform(get("/admin/users").param("page", "1").param("pageSize", "10"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(get("/admin/users/1"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(get("/admin/users/1/permissions"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(post("/admin/users").contentType(MediaType.APPLICATION_JSON).content(sysUserJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(put("/admin/users/1").contentType(MediaType.APPLICATION_JSON).content(sysUserJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(put("/admin/users/1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"permissionCodes":["dashboard:view","customers:view"]}
                                """))
                .andExpect(okWithCodeOne());
        mockMvc.perform(patch("/admin/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":1}
                                """))
                .andExpect(okWithCodeOne());
        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(get("/admin/permissions"))
                .andExpect(okWithCodeOne());
    }

    @Test
    void staffMemberEndpointsReturnSuccess() throws Exception {
        mockMvc.perform(get("/admin/staff-members").param("page", "1").param("pageSize", "10"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(get("/admin/staff-members/1"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(post("/admin/staff-members").contentType(MediaType.APPLICATION_JSON).content(staffJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(put("/admin/staff-members/1").contentType(MediaType.APPLICATION_JSON).content(staffJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(patch("/admin/staff-members/1/status").param("status", "1"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(delete("/admin/staff-members/1"))
                .andExpect(okWithCodeOne());
    }

    @Test
    void serviceProjectEndpointsReturnSuccess() throws Exception {
        mockMvc.perform(get("/admin/service-projects").param("page", "1").param("pageSize", "10"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(get("/admin/service-projects/1"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(post("/admin/service-projects").contentType(MediaType.APPLICATION_JSON).content(projectJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(put("/admin/service-projects/1").contentType(MediaType.APPLICATION_JSON).content(projectJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(patch("/admin/service-projects/1/status").param("status", "1"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(delete("/admin/service-projects/1"))
                .andExpect(okWithCodeOne());
    }

    @Test
    void serviceProjectInventoryEndpointsReturnSuccess() throws Exception {
        mockMvc.perform(get("/admin/service-project-inventories").param("page", "1").param("pageSize", "10"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(get("/admin/service-project-inventories/1"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(post("/admin/service-project-inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(serviceProjectInventoryJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(put("/admin/service-project-inventories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(serviceProjectInventoryJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(delete("/admin/service-project-inventories/1"))
                .andExpect(okWithCodeOne());
    }

    @Test
    void customerProfileEndpointsReturnSuccess() throws Exception {
        mockMvc.perform(get("/admin/customers").param("page", "1").param("pageSize", "10"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(get("/admin/customers/1"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(post("/admin/customers").contentType(MediaType.APPLICATION_JSON).content(customerJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(put("/admin/customers/1").contentType(MediaType.APPLICATION_JSON).content(customerJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(delete("/admin/customers/1"))
                .andExpect(okWithCodeOne());
    }

    @Test
    void inventorySkuEndpointsReturnSuccess() throws Exception {
        mockMvc.perform(get("/admin/inventory-skus").param("page", "1").param("pageSize", "10"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(get("/admin/inventory-skus/export").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/inventory-skus/1"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(post("/admin/inventory-skus").contentType(MediaType.APPLICATION_JSON).content(inventorySkuJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(put("/admin/inventory-skus/1").contentType(MediaType.APPLICATION_JSON).content(inventorySkuJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(patch("/admin/inventory-skus/1/status").param("status", "1"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(delete("/admin/inventory-skus/1"))
                .andExpect(okWithCodeOne());
    }

    @Test
    void appointmentEndpointsReturnSuccess() throws Exception {
        mockMvc.perform(get("/admin/appointments").param("page", "1").param("pageSize", "10"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(get("/admin/appointments/1"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(post("/admin/appointments").contentType(MediaType.APPLICATION_JSON).content(appointmentJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(put("/admin/appointments/1").contentType(MediaType.APPLICATION_JSON).content(appointmentJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(patch("/admin/appointments/1/confirm"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(patch("/admin/appointments/1/cancel"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(delete("/admin/appointments/1"))
                .andExpect(okWithCodeOne());
    }

    @Test
    void appointmentItemEndpointsReturnSuccess() throws Exception {
        mockMvc.perform(get("/admin/appointment-items").param("appointmentId", "1"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(post("/admin/appointment-items").contentType(MediaType.APPLICATION_JSON).content(appointmentItemJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(put("/admin/appointment-items/1").contentType(MediaType.APPLICATION_JSON).content(appointmentItemJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(delete("/admin/appointment-items/1"))
                .andExpect(okWithCodeOne());
    }

    @Test
    void serviceOrderEndpointsReturnSuccess() throws Exception {
        mockMvc.perform(get("/admin/service-orders").param("page", "1").param("pageSize", "10"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(get("/admin/service-orders/1"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(post("/admin/service-orders").contentType(MediaType.APPLICATION_JSON).content(serviceOrderJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(post("/admin/service-orders/from-appointment/1"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(put("/admin/service-orders/1").contentType(MediaType.APPLICATION_JSON).content(serviceOrderJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(patch("/admin/service-orders/1/cancel"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(patch("/admin/service-orders/1/finish"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(delete("/admin/service-orders/1"))
                .andExpect(okWithCodeOne());
    }

    @Test
    void serviceOrderItemEndpointsReturnSuccess() throws Exception {
        mockMvc.perform(get("/admin/service-order-items").param("orderId", "1"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(post("/admin/service-order-items").contentType(MediaType.APPLICATION_JSON).content(serviceOrderItemJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(put("/admin/service-order-items/1").contentType(MediaType.APPLICATION_JSON).content(serviceOrderItemJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(delete("/admin/service-order-items/1"))
                .andExpect(okWithCodeOne());
    }

    @Test
    void paymentRecordEndpointsReturnSuccess() throws Exception {
        mockMvc.perform(get("/admin/payment-records").param("page", "1").param("pageSize", "10"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(get("/admin/payment-records/1"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(post("/admin/payment-records").contentType(MediaType.APPLICATION_JSON).content(paymentRecordJson()))
                .andExpect(okWithCodeOne());
        mockMvc.perform(patch("/admin/payment-records/1/void"))
                .andExpect(okWithCodeOne());
    }

    @Test
    void inventoryStockLogEndpointsReturnSuccess() throws Exception {
        mockMvc.perform(get("/admin/inventory-stock-logs").param("page", "1").param("pageSize", "10"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(get("/admin/inventory-stock-logs/1"))
                .andExpect(okWithCodeOne());
        mockMvc.perform(post("/admin/inventory-stock-logs").contentType(MediaType.APPLICATION_JSON).content(stockLogJson("stock_in")))
                .andExpect(okWithCodeOne());
        mockMvc.perform(post("/admin/inventory-stock-logs/inbound").contentType(MediaType.APPLICATION_JSON).content(stockLogJson("stock_in")))
                .andExpect(okWithCodeOne());
        mockMvc.perform(post("/admin/inventory-stock-logs/outbound").contentType(MediaType.APPLICATION_JSON).content(stockLogJson("stock_out")))
                .andExpect(okWithCodeOne());
        mockMvc.perform(post("/admin/inventory-stock-logs/adjust").contentType(MediaType.APPLICATION_JSON).content(stockLogJson("check")))
                .andExpect(okWithCodeOne());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubCommonServiceMethods() {
        when(passwordEncoder.encode(any())).thenReturn("$2a$encoded");

        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setId(1L);
        loginUserVO.setUsername("admin");
        loginUserVO.setToken("token");
        when(sysUserService.login(any(SysUserLoginDTO.class))).thenReturn(loginUserVO);
        when(sysUserService.page(any(SysUserPageQueryDTO.class))).thenReturn(pageResult(sysUserVO()));
        when(sysUserService.getByID(anyLong())).thenReturn(sysUserVO());
        when(sysUserService.getById(anyLong())).thenReturn(sysUser());
        when(permissionPointService.listPermissionPoints()).thenReturn(List.of(new PermissionPointVO()));
        when(permissionPointService.getUserPermissions(anyLong())).thenReturn(new UserPermissionVO());

        when(appointmentService.List(any(AppointmentPageQueryDTO.class))).thenReturn(Result.success(pageResult(new AppointmentVO())));
        when(appointmentService.getByID(anyLong())).thenReturn(new AppointmentVO());
        when(appointmentService.createAppointment(any())).thenReturn(true);
        when(appointmentService.updateAppointment(anyLong(), any())).thenReturn(true);
        when(appointmentService.confirm(anyLong())).thenReturn(true);
        when(appointmentService.complete(anyLong())).thenReturn(true);
        when(appointmentService.cancel(anyLong())).thenReturn(true);
        when(appointmentService.deleteAppointment(anyLong())).thenReturn(true);

        when(staffMemberService.page(any(Page.class), any(Wrapper.class))).thenReturn(pageOf(staffMember()));
        when(serviceProjectService.page(any(Page.class), any(Wrapper.class))).thenReturn(pageOf(serviceProject()));
        when(serviceProjectInventoryService.page(any(Page.class), any(Wrapper.class)))
                .thenReturn(pageOf(serviceProjectInventory()));
        when(customerProfileService.page(any(Page.class), any(Wrapper.class))).thenReturn(pageOf(customerProfile()));
        when(inventorySkuService.page(any(Page.class), any(Wrapper.class))).thenReturn(pageOf(inventorySku()));
        when(inventoryStockLogService.page(any(Page.class), any(Wrapper.class))).thenReturn(pageOf(stockLog("stock_in")));
        when(paymentRecordService.page(any(Page.class), any(Wrapper.class))).thenReturn(pageOf(paymentRecord(1)));
        when(serviceOrderService.page(any(Page.class), any(Wrapper.class))).thenReturn(pageOf(serviceOrder()));
        when(serviceOrderService.pageOrders(any(ServiceOrderPageQueryDTO.class))).thenReturn(pageResult(serviceOrderVO()));

        when(staffMemberService.getById(anyLong())).thenAnswer(invocation -> staffMember());
        when(serviceProjectService.getById(anyLong())).thenAnswer(invocation -> serviceProject());
        when(serviceProjectInventoryService.getById(anyLong())).thenAnswer(invocation -> serviceProjectInventory());
        when(customerProfileService.getById(anyLong())).thenAnswer(invocation -> customerProfile());
        when(inventorySkuService.getById(anyLong())).thenAnswer(invocation -> inventorySku());
        when(inventorySkuService.getOne(any(Wrapper.class))).thenAnswer(invocation -> inventorySku());
        when(inventoryStockLogService.getById(anyLong())).thenAnswer(invocation -> stockLog("stock_in"));
        when(inventoryStockLogService.recordStockChange(any(), any())).thenAnswer(invocation -> stockLog("stock_in"));
        when(paymentRecordService.getById(anyLong())).thenReturn(paymentRecord(1), paymentRecord(0), paymentRecord(0));
        when(paymentRecordService.createPaymentRecord(any())).thenReturn(1L);
        when(paymentRecordService.voidPaymentRecord(anyLong())).thenReturn(true);
        when(serviceOrderService.getById(anyLong())).thenAnswer(invocation -> serviceOrder());
        when(serviceOrderService.getDetail(anyLong())).thenReturn(serviceOrderVO());
        when(appointmentService.getById(anyLong())).thenAnswer(invocation -> appointment());

        when(appointmentItemService.list(any(Wrapper.class))).thenReturn(List.of(appointmentItem()));
        when(serviceOrderItemService.list(any(Wrapper.class))).thenReturn(List.of(serviceOrderItem()));
        when(customerProfileService.list(any(Wrapper.class))).thenReturn(List.of(customerProfile()));
        when(inventorySkuService.list(any(Wrapper.class))).thenReturn(List.of(inventorySku()));

        when(paymentRecordService.count(any(Wrapper.class))).thenReturn(0L);
        when(serviceProjectInventoryService.count(any(Wrapper.class))).thenReturn(0L);
        when(inventorySkuService.count(any(Wrapper.class))).thenReturn(1L);
        when(serviceOrderService.getOrderItemsByOrderId(anyLong())).thenReturn(List.of(new ServiceOrderItemVO()));
        when(serviceOrderService.getPaymentRecordByOrderId(anyLong())).thenReturn(List.of(new PaymentRecordVO()));
        when(serviceOrderService.createOrder(any(ServiceOrderDTO.class))).thenReturn(1L);
        when(serviceOrderService.createFromAppointment(anyLong())).thenReturn(1L);
        when(serviceOrderService.updateOrder(anyLong(), any(ServiceOrderDTO.class))).thenReturn(true);
        when(serviceOrderService.cancel(anyLong())).thenReturn(true);
        when(serviceOrderService.finish(anyLong())).thenReturn(true);
        when(serviceOrderService.deleteOrder(anyLong())).thenReturn(true);

        stubSave(sysUserService);
        stubSave(staffMemberService);
        stubSave(serviceProjectService);
        stubSave(serviceProjectInventoryService);
        stubSave(customerProfileService);
        stubSave(inventorySkuService);
        stubSave(inventoryStockLogService);
        stubSave(appointmentService);
        stubSave(appointmentItemService);
        stubSave(serviceOrderService);
        stubSave(serviceOrderItemService);
        stubSave(paymentRecordService);

        when(staffMemberService.updateById(any(StaffMember.class))).thenReturn(true);
        when(serviceProjectService.updateById(any(ServiceProject.class))).thenReturn(true);
        when(serviceProjectInventoryService.updateById(any(ServiceProjectInventory.class))).thenReturn(true);
        when(customerProfileService.updateById(any(CustomerProfile.class))).thenReturn(true);
        when(inventorySkuService.updateById(any(InventorySku.class))).thenReturn(true);
        when(appointmentService.updateById(any(Appointment.class))).thenReturn(true);
        when(appointmentItemService.updateById(any(AppointmentItem.class))).thenReturn(true);
        when(serviceOrderService.updateById(any(ServiceOrder.class))).thenReturn(true);
        when(serviceOrderItemService.updateById(any(ServiceOrderItem.class))).thenReturn(true);
        when(paymentRecordService.updateById(any(PaymentRecord.class))).thenReturn(true);
        when(sysUserService.updateById(any(SysUser.class))).thenReturn(true);

        when(serviceOrderItemService.saveBatch(anyCollection())).thenReturn(true);
        when(serviceOrderItemService.remove(any(Wrapper.class))).thenReturn(true);

        when(sysUserService.removeById(anyLong())).thenReturn(true);
        when(staffMemberService.removeById(anyLong())).thenReturn(true);
        when(serviceProjectService.removeById(anyLong())).thenReturn(true);
        when(serviceProjectInventoryService.removeById(anyLong())).thenReturn(true);
        when(customerProfileService.removeById(anyLong())).thenReturn(true);
        when(inventorySkuService.removeById(anyLong())).thenReturn(true);
        when(appointmentService.removeById(anyLong())).thenReturn(true);
        when(appointmentItemService.removeById(anyLong())).thenReturn(true);
        when(serviceOrderService.removeById(anyLong())).thenReturn(true);
        when(serviceOrderItemService.removeById(anyLong())).thenReturn(true);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void stubSave(IService service) {
        doAnswer(invocation -> {
            assignIdIfPossible(invocation.getArgument(0));
            return true;
        }).when(service).save(any());
    }

    private void assignIdIfPossible(Object entity) throws Exception {
        Method getId = entity.getClass().getMethod("getId");
        if (getId.invoke(entity) == null) {
            Method setId = entity.getClass().getMethod("setId", Long.class);
            setId.invoke(entity, 1L);
        }
    }

    private ResultMatcher okWithCodeOne() {
        return result -> {
            status().isOk().match(result);
            jsonPath("$.code").value(200).match(result);
        };
    }

    private <T> Page<T> pageOf(T record) {
        Page<T> page = new Page<>(1, 10);
        page.setRecords(List.of(record));
        page.setTotal(1);
        return page;
    }

    private <T> PageResult<T> pageResult(T record) {
        return PageResult.<T>builder()
                .total(1)
                .records(List.of(record))
                .build();
    }

    private SysUserVO sysUserVO() {
        SysUserVO vo = new SysUserVO();
        vo.setId(1L);
        vo.setUsername("admin");
        vo.setRoleId(1);
        vo.setStatus(1);
        return vo;
    }

    private SysUser sysUser() {
        SysUser entity = new SysUser();
        entity.setId(1L);
        entity.setUsername("admin");
        entity.setRoleId(1);
        entity.setStatus(1);
        return entity;
    }

    private StaffMember staffMember() {
        StaffMember entity = new StaffMember();
        entity.setId(1L);
        entity.setName("staff");
        entity.setGender(1);
        entity.setStatus(1);
        return entity;
    }

    private ServiceProject serviceProject() {
        ServiceProject entity = new ServiceProject();
        entity.setId(1L);
        entity.setName("project");
        entity.setPrice(BigDecimal.valueOf(100));
        entity.setStatus(1);
        return entity;
    }

    private CustomerProfile customerProfile() {
        CustomerProfile entity = new CustomerProfile();
        entity.setId(1L);
        entity.setName("customer");
        entity.setGender(1);
        entity.setLevel(1);
        return entity;
    }

    private InventorySku inventorySku() {
        InventorySku entity = new InventorySku();
        entity.setId(1L);
        entity.setName("sku");
        entity.setUnit("piece");
        entity.setQuantity(BigDecimal.TEN);
        entity.setSafetyStock(BigDecimal.ONE);
        entity.setStatus(1);
        return entity;
    }

    private ServiceProjectInventory serviceProjectInventory() {
        ServiceProjectInventory entity = new ServiceProjectInventory();
        entity.setId(1L);
        entity.setServiceProjectId(1L);
        entity.setInventoryId(1L);
        entity.setConsumeQuantity(BigDecimal.ONE);
        entity.setStatus(1);
        return entity;
    }

    private InventoryStockLog stockLog(String changeType) {
        InventoryStockLog entity = new InventoryStockLog();
        entity.setId(1L);
        entity.setInventoryId(1L);
        entity.setChangeType(changeType);
        entity.setChangeQuantity(BigDecimal.ONE);
        entity.setBeforeQuantity(BigDecimal.TEN);
        entity.setAfterQuantity(BigDecimal.valueOf(11));
        return entity;
    }

    private Appointment appointment() {
        Appointment entity = new Appointment();
        entity.setId(1L);
        entity.setAppointmentNo("A001");
        entity.setCustomerId(1L);
        entity.setStaffId(1L);
        entity.setAppointmentTime(LocalDateTime.now().plusDays(1));
        entity.setStatus(0);
        entity.setTotalDurationMinutes(60);
        return entity;
    }

    private AppointmentItem appointmentItem() {
        AppointmentItem entity = new AppointmentItem();
        entity.setId(1L);
        entity.setAppointmentId(1L);
        entity.setServiceProjectId(1L);
        entity.setStaffId(1L);
        entity.setServiceName("project");
        entity.setPrice(BigDecimal.valueOf(100));
        entity.setDurationMinutes(60);
        return entity;
    }

    private ServiceOrder serviceOrder() {
        ServiceOrder entity = new ServiceOrder();
        entity.setId(1L);
        entity.setOrderNo("O001");
        entity.setCustomerId(1L);
        entity.setOrderType("service");
        entity.setOriginalAmount(BigDecimal.valueOf(100));
        entity.setDiscountAmount(BigDecimal.ZERO);
        entity.setReceivableAmount(BigDecimal.valueOf(100));
        entity.setPaidAmount(BigDecimal.ZERO);
        entity.setDebtAmount(BigDecimal.valueOf(100));
        entity.setDebtStatus(1);
        entity.setPayStatus(0);
        entity.setOrderStatus(0);
        return entity;
    }

    private ServiceOrderVO serviceOrderVO() {
        ServiceOrderVO vo = new ServiceOrderVO();
        vo.setId(1L);
        vo.setOrderNo("O001");
        vo.setCustomerId(1L);
        vo.setCustomerName("customer");
        vo.setOrderType("service");
        vo.setOriginalAmount(BigDecimal.valueOf(100));
        vo.setDiscountAmount(BigDecimal.ZERO);
        vo.setReceivableAmount(BigDecimal.valueOf(100));
        vo.setPaidAmount(BigDecimal.ZERO);
        vo.setDebtAmount(BigDecimal.valueOf(100));
        vo.setDebtStatus(1);
        vo.setPayStatus(0);
        vo.setOrderStatus(0);
        return vo;
    }

    private ServiceOrderItem serviceOrderItem() {
        ServiceOrderItem entity = new ServiceOrderItem();
        entity.setId(1L);
        entity.setOrderId(1L);
        entity.setServiceProjectId(1L);
        entity.setServiceName("project");
        entity.setUnitPrice(BigDecimal.valueOf(100));
        entity.setQuantity(BigDecimal.ONE);
        entity.setDiscountAmount(BigDecimal.ZERO);
        entity.setActualAmount(BigDecimal.valueOf(100));
        entity.setStaffId(1L);
        return entity;
    }

    private PaymentRecord paymentRecord(Integer status) {
        PaymentRecord entity = new PaymentRecord();
        entity.setId(1L);
        entity.setOrderId(1L);
        entity.setPaymentNo("P001");
        entity.setPaymentMethod("wechat");
        entity.setPayAmount(BigDecimal.TEN);
        entity.setPayStatus(status);
        entity.setOperatorId(1L);
        return entity;
    }

    private String sysUserJson() {
        return """
                {"username":"admin","passwordHash":"123456","roleId":1,"staffId":1,"status":1}
                """;
    }

    private String staffJson() {
        return """
                {"name":"staff","phone":"13800000000","gender":1,"position":"operator","status":1,"remark":"ok"}
                """;
    }

    private String projectJson() {
        return """
                {"name":"project","category":"face","price":100,"durationMinutes":60,"description":"desc","status":1}
                """;
    }

    private String serviceProjectInventoryJson() {
        return """
                {"serviceProjectId":1,"inventoryId":1,"consumeQuantity":1,"status":1,"remark":"ok"}
                """;
    }

    private String customerJson() {
        return """
                {"name":"customer","phone":"13900000000","gender":1,"birthday":"1990-01-01","level":1,"source":"walk_in","remark":"ok"}
                """;
    }

    private String inventorySkuJson() {
        return """
                {"name":"sku","category":"material","unit":"piece","quantity":10,"safetyStock":1,"costPrice":2,"supplier":"supplier","status":1,"remark":"ok"}
                """;
    }

    private String appointmentJson() {
        return """
                {"appointmentNo":"A001","customerId":1,"staffId":1,"appointmentTime":"2026-07-08T10:00:00","status":0,"totalDurationMinutes":60,"remark":"ok","items":[{"serviceProjectId":1,"staffId":1,"serviceName":"project","price":100,"durationMinutes":60,"sortNo":1}]}
                """;
    }

    private String appointmentItemJson() {
        return """
                {"appointmentId":1,"serviceProjectId":1,"staffId":1,"serviceName":"project","price":100,"durationMinutes":60,"sortNo":1}
                """;
    }

    private String serviceOrderJson() {
        return """
                {"orderNo":"O001","customerId":1,"orderType":"service","originalAmount":100,"discountAmount":0,"receivableAmount":100,"paidAmount":0,"debtAmount":100,"debtStatus":1,"payStatus":0,"orderStatus":0,"remark":"ok","items":[{"serviceProjectId":1,"serviceName":"project","unitPrice":100,"quantity":1,"discountAmount":0,"actualAmount":100,"staffId":1,"remark":"ok"}]}
                """;
    }

    private String serviceOrderItemJson() {
        return """
                {"orderId":1,"serviceProjectId":1,"serviceName":"project","unitPrice":100,"quantity":1,"discountAmount":0,"actualAmount":100,"staffId":1,"remark":"ok"}
                """;
    }

    private String paymentRecordJson() {
        return """
                {"orderId":1,"paymentMethod":"wechat","payAmount":10,"payStatus":1,"operatorId":1,"remark":"ok"}
                """;
    }

    private String stockLogJson(String changeType) {
        return """
                {"inventoryId":1,"changeType":"%s","changeQuantity":1,"relatedOrderId":1,"operatorId":1,"remark":"ok"}
                """.formatted(changeType);
    }
}
