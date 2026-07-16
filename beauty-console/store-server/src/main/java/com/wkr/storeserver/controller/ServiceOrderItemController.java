package com.wkr.storeserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.exception.SystemException;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.ServiceOrderItemDTO;
import com.wkr.storepojo.entity.ServiceOrderItem;
import com.wkr.storepojo.entity.StaffMember;
import com.wkr.storepojo.vo.ServiceOrderItemVO;
import com.wkr.storeserver.service.ServiceOrderItemService;
import com.wkr.storeserver.service.ServiceOrderService;
import com.wkr.storeserver.service.StaffMemberService;
import com.wkr.storeserver.support.DataScopeSupport;
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
import java.util.List;

/**
 * 订单项目明细接口控制器，负责接收管理端请求、校验参数并调用服务层完成业务处理。
 */
@Slf4j
@RestController
@RequestMapping("/admin/service-order-items")
@Tag(name = "订单项目相关接口")
public class ServiceOrderItemController {

    private final ServiceOrderItemService serviceOrderItemService;
    private final StaffMemberService staffMemberService;
    private final ServiceOrderService serviceOrderService;

    public ServiceOrderItemController(
            ServiceOrderItemService serviceOrderItemService,
            StaffMemberService staffMemberService,
            ServiceOrderService serviceOrderService) {
        this.serviceOrderItemService = serviceOrderItemService;
        this.staffMemberService = staffMemberService;
        this.serviceOrderService = serviceOrderService;
    }

    @GetMapping
    @Operation(summary = "订单项目列表")
    public Result<List<ServiceOrderItemVO>> list(@RequestParam("orderId") Long orderId) {
        serviceOrderService.assertCanAccess(orderId);
        LambdaQueryWrapper<ServiceOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceOrderItem::getOrderId, orderId);

        List<ServiceOrderItemVO> itemVOList = serviceOrderItemService.list(wrapper).stream()
                .map(this::toVO)
                .toList();

        return Result.success(itemVOList);
    }

    @PostMapping
    @Operation(summary = "创建服务订单项目")
    public Result<Long> create(@Valid @RequestBody ServiceOrderItemDTO dto) {
        serviceOrderService.assertCanAccess(dto.getOrderId());
        ServiceOrderItem serviceOrderItem = new ServiceOrderItem();
        BeanUtils.copyProperties(dto, serviceOrderItem);
        applyStaffScope(serviceOrderItem);
        serviceOrderItem.setCreateTime(LocalDateTime.now());
        serviceOrderItemService.save(serviceOrderItem);

        return Result.success(serviceOrderItem.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改订单项目")
    public Result<Boolean> update(@PathVariable("id") Long id, @Valid @RequestBody ServiceOrderItemDTO dto) {
        ServiceOrderItem existing = requireItem(id);
        serviceOrderService.assertCanAccess(existing.getOrderId());
        assertCanModifyItem(existing);
        serviceOrderService.assertCanAccess(dto.getOrderId());
        ServiceOrderItem serviceOrderItem = new ServiceOrderItem();
        BeanUtils.copyProperties(dto, serviceOrderItem);
        serviceOrderItem.setId(id);
        applyStaffScope(serviceOrderItem);

        boolean updated = serviceOrderItemService.updateById(serviceOrderItem);
        if (!updated) {
            throw new SystemException("修改订单项目失败");
        }
        return Result.success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除订单项目")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        ServiceOrderItem existing = requireItem(id);
        serviceOrderService.assertCanAccess(existing.getOrderId());
        assertCanModifyItem(existing);
        boolean removed = serviceOrderItemService.removeById(id);
        if (!removed) {
            throw new SystemException("删除订单项目失败");
        }
        return Result.success(true);
    }

    private ServiceOrderItem requireItem(Long id) {
        ServiceOrderItem item = serviceOrderItemService.getById(id);
        if (item == null) {
            throw new BusinessException("订单项目明细不存在");
        }
        return item;
    }

    private void applyStaffScope(ServiceOrderItem serviceOrderItem) {
        Long staffId = DataScopeSupport.currentScopedStaffId();
        if (staffId != null) {
            serviceOrderItem.setStaffId(staffId);
        }
    }

    private void assertCanModifyItem(ServiceOrderItem item) {
        Long staffId = DataScopeSupport.currentScopedStaffId();
        if (staffId != null && !staffId.equals(item.getStaffId())) {
            throw new BusinessException("普通员工不能修改其他员工的订单项目");
        }
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
