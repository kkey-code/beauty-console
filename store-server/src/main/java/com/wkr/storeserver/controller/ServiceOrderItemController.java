package com.wkr.storeserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wkr.storecommon.common.Result;
import com.wkr.storepojo.dto.ServiceOrderItemDTO;
import com.wkr.storepojo.entity.ServiceOrderItem;
import com.wkr.storepojo.entity.StaffMember;
import com.wkr.storepojo.vo.ServiceOrderItemVO;
import com.wkr.storeserver.service.ServiceOrderItemService;
import com.wkr.storeserver.service.StaffMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/service-order-items")
@Api(tags = "订单项目相关接口")
public class ServiceOrderItemController {

    private final ServiceOrderItemService serviceOrderItemService;
    private final StaffMemberService staffMemberService;

    public ServiceOrderItemController(
            ServiceOrderItemService serviceOrderItemService,
            StaffMemberService staffMemberService) {
        this.serviceOrderItemService = serviceOrderItemService;
        this.staffMemberService = staffMemberService;
    }

    @GetMapping
    @ApiOperation("订单项目列表")
    public Result<List<ServiceOrderItemVO>> list(@RequestParam Long orderId) {
        LambdaQueryWrapper<ServiceOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceOrderItem::getOrderId, orderId);

        List<ServiceOrderItemVO> itemVOList = serviceOrderItemService.list(wrapper).stream()
                .map(this::toVO)
                .toList();

        return Result.success(itemVOList);
    }

    @PostMapping
    @ApiOperation("创建服务订单项目")
    public Result<Long> create(@Valid @RequestBody ServiceOrderItemDTO dto) {
        ServiceOrderItem serviceOrderItem = new ServiceOrderItem();
        BeanUtils.copyProperties(dto, serviceOrderItem);
        serviceOrderItem.setCreateTime(LocalDateTime.now());
        serviceOrderItemService.save(serviceOrderItem);

        return Result.success(serviceOrderItem.getId());
    }

    @PutMapping("/{id}")
    @ApiOperation("修改订单项目")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody ServiceOrderItemDTO dto) {
        ServiceOrderItem serviceOrderItem = new ServiceOrderItem();
        BeanUtils.copyProperties(dto, serviceOrderItem);
        serviceOrderItem.setId(id);

        boolean updated = serviceOrderItemService.updateById(serviceOrderItem);
        if (!updated) {
            return Result.error("修改失败");
        }
        return Result.success(true);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除订单项目")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = serviceOrderItemService.removeById(id);
        if (!removed) {
            return Result.error("删除失败");
        }
        return Result.success(true);
    }

    private ServiceOrderItemVO toVO(ServiceOrderItem item) {
        ServiceOrderItemVO itemVO = new ServiceOrderItemVO();
        BeanUtils.copyProperties(item, itemVO);

        if (item.getStaffId() != null) {
            StaffMember staffMember = staffMemberService.getById(item.getStaffId());
            if (staffMember != null) {
                itemVO.setStaffName(staffMember.getName());
            }
        }
        return itemVO;
    }
}
