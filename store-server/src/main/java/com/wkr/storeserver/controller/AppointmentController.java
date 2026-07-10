package com.wkr.storeserver.controller;

import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.AppointmentDTO;
import com.wkr.storepojo.dto.AppointmentItemDTO;
import com.wkr.storepojo.dto.AppointmentPageQueryDTO;
import com.wkr.storepojo.entity.Appointment;
import com.wkr.storepojo.entity.AppointmentItem;
import com.wkr.storepojo.enums.AppointmentStatusEnum;
import com.wkr.storepojo.vo.AppointmentVO;
import com.wkr.storeserver.service.AppointmentItemService;
import com.wkr.storeserver.service.AppointmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 预约接口控制器，负责接收管理端请求、校验参数并调用服务层完成业务处理。
 */
@Slf4j
@RestController
@RequestMapping("/admin/appointments")
@Api(tags = "预约相关接口")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentItemService appointmentItemService;

    public AppointmentController(AppointmentService appointmentService, AppointmentItemService appointmentItemService) {
        this.appointmentService = appointmentService;
        this.appointmentItemService = appointmentItemService;
    }

    @GetMapping
    @ApiOperation("预约列表")
    public Result<PageResult<AppointmentVO>> list(AppointmentPageQueryDTO dto) {
        return appointmentService.List(dto);
    }

    @GetMapping("/{id}")
    @ApiOperation("预约详情")
    public Result<AppointmentVO> getByID(@PathVariable("id") Long id) {
        return Result.success(appointmentService.getByID(id));
    }

    @PostMapping
    @ApiOperation("新增预约")
    @Transactional
    public Result<Boolean> add(@Valid @RequestBody AppointmentDTO dto) {
        Appointment appointment = new Appointment();
        BeanUtils.copyProperties(dto, appointment);
        appointment.setCreateTime(LocalDateTime.now());
        appointment.setUpdateTime(LocalDateTime.now());

        boolean saved = appointmentService.save(appointment);
        if (!saved) {
            throw new BusinessException("新增预约失败");
        }

        if (dto.getItems() != null) {
            for (AppointmentItemDTO item : dto.getItems()) {
                AppointmentItem appointmentItem = new AppointmentItem();
                BeanUtils.copyProperties(item, appointmentItem);
                appointmentItem.setAppointmentId(appointment.getId());
                appointmentItem.setCreateTime(LocalDateTime.now());
                appointmentItemService.save(appointmentItem);
            }
        }

        return Result.success(true);
    }

    @PutMapping("/{id}")
    @ApiOperation("修改预约")
    public Result<Boolean> update(@PathVariable("id") Long id, @Valid @RequestBody AppointmentDTO dto) {
        Appointment appointment = new Appointment();
        BeanUtils.copyProperties(dto, appointment);
        appointment.setId(id);
        appointment.setUpdateTime(LocalDateTime.now());
        return Result.success(appointmentService.updateById(appointment));
    }

    @PatchMapping("/{id}/confirm")
    @ApiOperation("确认预约")
    public Result<Boolean> confirm(@PathVariable("id") Long id) {
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setStatus(AppointmentStatusEnum.CONFIRMED.getCode());
        appointment.setUpdateTime(LocalDateTime.now());

        return Result.success(appointmentService.updateById(appointment));
    }

    @PatchMapping("/{id}/cancel")
    @ApiOperation("取消预约")
    public Result<Boolean> cancel(@PathVariable("id") Long id) {
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setStatus(AppointmentStatusEnum.CANCELED.getCode());
        appointment.setUpdateTime(LocalDateTime.now());

        return Result.success(appointmentService.updateById(appointment));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除预约")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        return Result.success(appointmentService.removeById(id));
    }
}
