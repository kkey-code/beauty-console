package com.wkr.storeserver.controller;

import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storepojo.dto.ServiceOrderDTO;
import com.wkr.storepojo.dto.ServiceOrderPageQueryDTO;
import com.wkr.storepojo.vo.ServiceOrderVO;
import com.wkr.storeserver.audit.AuditLog;
import com.wkr.storeserver.ratelimit.RateLimiter;
import com.wkr.storeserver.service.ServiceOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 服务订单接口控制器，只负责接收管理端请求并委托服务层处理订单业务。
 */
@Slf4j
@RestController
@RequestMapping("/admin/service-orders")
@Tag(name = "订单相关接口")
public class ServiceOrderController {

    private final ServiceOrderService serviceOrderService;

    public ServiceOrderController(ServiceOrderService serviceOrderService) {
        this.serviceOrderService = serviceOrderService;
    }

    @GetMapping
    @Operation(summary = "订单列表")
    public Result<PageResult<ServiceOrderVO>> list(@Valid ServiceOrderPageQueryDTO dto) {
        return Result.success(serviceOrderService.pageOrders(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据订单 id 查询订单")
    public Result<ServiceOrderVO> getById(@PathVariable("id") Long id) {
        return Result.success(serviceOrderService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "添加订单")
    @AuditLog(action = "CREATE", target = "SERVICE_ORDER")
    @RateLimiter(key = "service-order:create", maxRequests = 10, window = 1, timeUnit = TimeUnit.MINUTES)
    public Result<Long> create(@Valid @RequestBody ServiceOrderDTO serviceOrderDTO) {
        return Result.success(serviceOrderService.createOrder(serviceOrderDTO));
    }

    @PostMapping("/from-appointment/{appointmentId}")
    @Operation(summary = "从预约生成订单")
    @AuditLog(action = "CREATE_FROM_APPOINTMENT", target = "SERVICE_ORDER")
    @RateLimiter(key = "service-order:create", maxRequests = 10, window = 1, timeUnit = TimeUnit.MINUTES)
    public Result<Long> createFromAppointment(@PathVariable("appointmentId") Long appointmentId) {
        return Result.success(serviceOrderService.createFromAppointment(appointmentId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改订单")
    @AuditLog(action = "UPDATE", target = "SERVICE_ORDER")
    public Result<Boolean> update(@PathVariable("id") Long id, @Valid @RequestBody ServiceOrderDTO serviceOrderDTO) {
        return Result.success(serviceOrderService.updateOrder(id, serviceOrderDTO));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    @AuditLog(action = "CANCEL", target = "SERVICE_ORDER")
    public Result<Boolean> cancel(@PathVariable("id") Long id) {
        return Result.success(serviceOrderService.cancel(id));
    }

    @PatchMapping("/{id}/finish")
    @Operation(summary = "完成订单")
    @AuditLog(action = "FINISH", target = "SERVICE_ORDER")
    public Result<Boolean> finish(@PathVariable("id") Long id) {
        return Result.success(serviceOrderService.finish(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除订单")
    @AuditLog(action = "DELETE", target = "SERVICE_ORDER")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        return Result.success(serviceOrderService.deleteOrder(id));
    }
}
