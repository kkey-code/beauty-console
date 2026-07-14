package com.wkr.storeserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.AppointmentItemDTO;
import com.wkr.storepojo.entity.AppointmentItem;
import com.wkr.storepojo.entity.ServiceProject;
import com.wkr.storepojo.vo.AppointmentItemVO;
import com.wkr.storeserver.service.AppointmentItemService;
import com.wkr.storeserver.service.ServiceProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 预约项目明细接口控制器，负责接收管理端请求、校验参数并调用服务层完成业务处理。
 */
@Slf4j
@RestController
@RequestMapping("/admin/appointment-items")
@Tag(name = "预约项目相关接口")
public class AppointmentItemController {

    private final AppointmentItemService appointmentItemService;
    private final ServiceProjectService serviceProjectService;

    public AppointmentItemController(
            AppointmentItemService appointmentItemService,
            ServiceProjectService serviceProjectService) {
        this.appointmentItemService = appointmentItemService;
        this.serviceProjectService = serviceProjectService;
    }

    @GetMapping
    @Operation(summary = "预约项目列表")
    public Result<List<AppointmentItemVO>> list(@RequestParam("appointmentId") Long appointmentId) {
        LambdaQueryWrapper<AppointmentItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppointmentItem::getAppointmentId, appointmentId);

        List<AppointmentItem> list = appointmentItemService.list(wrapper);

        List<AppointmentItemVO> voList = new ArrayList<>();
        for (AppointmentItem appointmentItem : list) {
            voList.add(toVO(appointmentItem));
        }
        return Result.success(voList);
    }

    @PostMapping
    @Operation(summary = "新增预约项目")
    public Result<Long> save(@Valid @RequestBody AppointmentItemDTO appointmentItemDTO) {
        AppointmentItem appointmentItem = new AppointmentItem();
        BeanUtils.copyProperties(appointmentItemDTO, appointmentItem);
        appointmentItem.setCreateTime(LocalDateTime.now());

        boolean saved = appointmentItemService.save(appointmentItem);
        if (!saved) {
            throw new BusinessException("添加预约项目明细失败");
        }
        return Result.success(appointmentItem.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改预约项目明细")
    public Result<Boolean> update(@PathVariable("id") Long id, @Valid @RequestBody AppointmentItemDTO appointmentItemDTO) {
        AppointmentItem appointmentItem = new AppointmentItem();
        BeanUtils.copyProperties(appointmentItemDTO, appointmentItem);
        appointmentItem.setId(id);

        boolean updated = appointmentItemService.updateById(appointmentItem);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除预约项目")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        boolean removed = appointmentItemService.removeById(id);
        return Result.success(removed);
    }

    private AppointmentItemVO toVO(AppointmentItem appointmentItem) {
        AppointmentItemVO appointmentItemVO = new AppointmentItemVO();
        BeanUtils.copyProperties(appointmentItem, appointmentItemVO);

        ServiceProject serviceProject = serviceProjectService.getById(appointmentItem.getServiceProjectId());
        if (serviceProject != null) {
            appointmentItemVO.setServiceName(serviceProject.getName());
        }
        return appointmentItemVO;
    }
}
