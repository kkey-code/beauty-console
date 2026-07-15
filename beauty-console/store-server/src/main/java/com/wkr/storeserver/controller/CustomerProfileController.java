package com.wkr.storeserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storecommon.exception.SystemException;
import com.wkr.storepojo.dto.CustomerProfileDTO;
import com.wkr.storepojo.dto.CustomerProfilePageQueryDTO;
import com.wkr.storepojo.entity.CustomerProfile;
import com.wkr.storepojo.enums.CustomerLevelEnum;
import com.wkr.storepojo.enums.GenderEnum;
import com.wkr.storepojo.vo.CustomerProfileVO;
import com.wkr.storeserver.audit.AuditLog;
import com.wkr.storeserver.service.CustomerProfileService;
import com.wkr.storeserver.service.DeletionGuardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户档案接口控制器，负责接收管理端请求、校验参数并调用服务层完成业务处理。
 */
@Slf4j
@RestController
@RequestMapping("/admin/customers")
@Tag(name = "客户相关接口")
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;
    private final DeletionGuardService deletionGuardService;

    public CustomerProfileController(
            CustomerProfileService customerProfileService,
            DeletionGuardService deletionGuardService) {
        this.customerProfileService = customerProfileService;
        this.deletionGuardService = deletionGuardService;
    }

    @GetMapping
    @Operation(summary = "分页查询客户列表")
    public Result<PageResult<CustomerProfileVO>> list(@Valid CustomerProfilePageQueryDTO dto) {
        Page<CustomerProfile> page = new Page<>(dto.getPage(), dto.getPageSize());

        LambdaQueryWrapper<CustomerProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getName()), CustomerProfile::getName, dto.getName())
                .eq(StringUtils.hasText(dto.getPhone()), CustomerProfile::getPhone, dto.getPhone())
                .eq(dto.getLevel() != null, CustomerProfile::getLevel, dto.getLevel())
                .eq(StringUtils.hasText(dto.getSource()), CustomerProfile::getSource, dto.getSource())
                .orderByDesc(CustomerProfile::getCreateTime)
                .orderByDesc(CustomerProfile::getId);

        IPage<CustomerProfile> pageResult = customerProfileService.page(page, wrapper);

        List<CustomerProfileVO> voList = new ArrayList<>();
        for (CustomerProfile customerProfile : pageResult.getRecords()) {
            CustomerProfileVO customerProfileVO = new CustomerProfileVO();
            BeanUtils.copyProperties(customerProfile, customerProfileVO);
            customerProfileVO.setGenderName(GenderEnum.labelOf(customerProfile.getGender()));
            customerProfileVO.setLevelName(CustomerLevelEnum.labelOf(customerProfile.getLevel()));
            voList.add(customerProfileVO);
        }

        PageResult<CustomerProfileVO> result = new PageResult<>();
        result.setRecords(voList);
        result.setTotal(pageResult.getTotal());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询客户详情")
    public Result<CustomerProfileVO> get(@PathVariable("id") Long id) {
        CustomerProfile customerProfile = customerProfileService.getById(id);
        if (customerProfile == null) {
            throw new BusinessException("客户不存在");
        }

        CustomerProfileVO customerProfileVO = new CustomerProfileVO();
        BeanUtils.copyProperties(customerProfile, customerProfileVO);
        customerProfileVO.setGenderName(GenderEnum.labelOf(customerProfile.getGender()));
        customerProfileVO.setLevelName(CustomerLevelEnum.labelOf(customerProfile.getLevel()));
        return Result.success(customerProfileVO);
    }

    @PostMapping
    @Operation(summary = "添加客户")
    @AuditLog(action = "CREATE", target = "CUSTOMER")
    public Result<?> add(@Valid @RequestBody CustomerProfileDTO dto) {
        CustomerProfile customerProfile = new CustomerProfile();
        BeanUtils.copyProperties(dto, customerProfile);
        customerProfile.setCreateTime(LocalDateTime.now());
        customerProfile.setUpdateTime(LocalDateTime.now());

        boolean saved = customerProfileService.save(customerProfile);
        if (!saved) {
            throw new SystemException("添加客户失败");
        }
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改客户")
    @AuditLog(action = "UPDATE", target = "CUSTOMER")
    public Result<Boolean> update(@PathVariable("id") Long id, @Valid @RequestBody CustomerProfileDTO dto) {
        CustomerProfile customerProfile = new CustomerProfile();
        BeanUtils.copyProperties(dto, customerProfile);
        customerProfile.setId(id);
        customerProfile.setUpdateTime(LocalDateTime.now());

        boolean updated = customerProfileService.updateById(customerProfile);
        if (!updated) {
            throw new BusinessException("客户不存在或修改失败");
        }
        return Result.success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除客户")
    @AuditLog(action = "DELETE", target = "CUSTOMER")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        deletionGuardService.assertCustomerCanDelete(id);
        boolean removed = customerProfileService.removeById(id);
        if (!removed) {
            throw new BusinessException("客户不存在或删除失败");
        }
        return Result.success(true);
    }
}
