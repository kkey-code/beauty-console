package com.wkr.storeserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.CustomerProfileDTO;
import com.wkr.storepojo.dto.CustomerProfilePageQueryDTO;
import com.wkr.storepojo.entity.CustomerProfile;
import com.wkr.storepojo.enums.CustomerLevelEnum;
import com.wkr.storepojo.enums.GenderEnum;
import com.wkr.storepojo.vo.CustomerProfileVO;
import com.wkr.storeserver.service.CustomerProfileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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

@Slf4j
@RestController
@RequestMapping("/admin/customers")
@Api(tags = "客户相关接口")
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;

    public CustomerProfileController(CustomerProfileService customerProfileService) {
        this.customerProfileService = customerProfileService;
    }

    @GetMapping
    @ApiOperation("分页查询客户列表")
    public Result<PageResult<CustomerProfileVO>> list(CustomerProfilePageQueryDTO dto) {
        Page<CustomerProfile> page = new Page<>(dto.getPage(), dto.getPageSize());

        LambdaQueryWrapper<CustomerProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getName()), CustomerProfile::getName, dto.getName())
                .eq(StringUtils.hasText(dto.getPhone()), CustomerProfile::getPhone, dto.getPhone())
                .eq(dto.getLevel() != null, CustomerProfile::getLevel, dto.getLevel())
                .eq(StringUtils.hasText(dto.getSource()), CustomerProfile::getSource, dto.getSource());

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
    @ApiOperation("查询客户详情")
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
    @ApiOperation("添加客户")
    public Result<?> add(@Valid @RequestBody CustomerProfileDTO dto) {
        CustomerProfile customerProfile = new CustomerProfile();
        BeanUtils.copyProperties(dto, customerProfile);
        customerProfile.setCreateTime(LocalDateTime.now());
        customerProfile.setUpdateTime(LocalDateTime.now());

        boolean saved = customerProfileService.save(customerProfile);
        return saved ? Result.success() : Result.error("添加客户失败");
    }

    @PutMapping("/{id}")
    @ApiOperation("修改客户")
    public Result<Boolean> update(@PathVariable("id") Long id, @Valid @RequestBody CustomerProfileDTO dto) {
        CustomerProfile customerProfile = new CustomerProfile();
        BeanUtils.copyProperties(dto, customerProfile);
        customerProfile.setId(id);
        customerProfile.setUpdateTime(LocalDateTime.now());

        boolean updated = customerProfileService.updateById(customerProfile);
        return updated ? Result.success(true) : Result.error("修改客户失败");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除客户")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        boolean removed = customerProfileService.removeById(id);
        return removed ? Result.success(true) : Result.error("删除客户失败");
    }
}
