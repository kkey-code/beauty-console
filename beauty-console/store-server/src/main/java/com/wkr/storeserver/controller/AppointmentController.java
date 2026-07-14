package com.wkr.storeserver.controller;

import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storepojo.dto.AppointmentDTO;
import com.wkr.storepojo.dto.AppointmentPageQueryDTO;
import com.wkr.storepojo.vo.AppointmentVO;
import com.wkr.storeserver.audit.AuditLog;
import com.wkr.storeserver.service.AppointmentService;
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

/**
 * 预约接口控制器，负责接收管理端请求、校验参数并调用服务层完成业务处理。
 */
@Slf4j
@RestController
@RequestMapping("/admin/appointments")
@Tag(name = "预约相关接口")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    @Operation(summary = "预约列表")
    public Result<PageResult<AppointmentVO>> list(AppointmentPageQueryDTO dto) {
        return appointmentService.List(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "预约详情")
    public Result<AppointmentVO> getByID(@PathVariable("id") Long id) {
        return Result.success(appointmentService.getByID(id));
    }

    @PostMapping
    @Operation(summary = "新增预约")
    @AuditLog(action = "CREATE", target = "APPOINTMENT")
    public Result<Boolean> add(@Valid @RequestBody AppointmentDTO dto) {
        return Result.success(appointmentService.createAppointment(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改预约")
    @AuditLog(action = "UPDATE", target = "APPOINTMENT")
    public Result<Boolean> update(@PathVariable("id") Long id, @Valid @RequestBody AppointmentDTO dto) {
        return Result.success(appointmentService.updateAppointment(id, dto));
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "确认预约")
    @AuditLog(action = "CONFIRM", target = "APPOINTMENT")
    public Result<Boolean> confirm(@PathVariable("id") Long id) {
        return Result.success(appointmentService.confirm(id));
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "完成预约")
    @AuditLog(action = "COMPLETE", target = "APPOINTMENT")
    public Result<Boolean> complete(@PathVariable("id") Long id) {
        return Result.success(appointmentService.complete(id));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "取消预约")
    @AuditLog(action = "CANCEL", target = "APPOINTMENT")
    public Result<Boolean> cancel(@PathVariable("id") Long id) {
        return Result.success(appointmentService.cancel(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除预约")
    @AuditLog(action = "DELETE", target = "APPOINTMENT")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        return Result.success(appointmentService.deleteAppointment(id));
    }
}
