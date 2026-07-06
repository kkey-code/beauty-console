package com.wkr.storeserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.ServiceProjectDTO;
import com.wkr.storepojo.dto.ServiceProjectPageQueryDTO;
import com.wkr.storepojo.entity.ServiceProject;
import com.wkr.storepojo.enums.CommonStatusEnum;
import com.wkr.storepojo.vo.ServiceProjectVO;
import com.wkr.storeserver.service.ServiceProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

@Slf4j
@RestController
@RequestMapping("/admin/service-projects")
@Api(tags = "服务项目相关接口")
public class ServiceProjectController {

    private final ServiceProjectService serviceProjectService;

    public ServiceProjectController(ServiceProjectService serviceProjectService) {
        this.serviceProjectService = serviceProjectService;
    }

    @GetMapping
    @ApiOperation("分页查询服务项目")
    public Result<PageResult<ServiceProjectVO>> list(ServiceProjectPageQueryDTO dto) {
        Page<ServiceProject> page = new Page<>(dto.getPage(), dto.getPageSize());

        LambdaQueryWrapper<ServiceProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getName()), ServiceProject::getName, dto.getName())
                .eq(StringUtils.hasText(dto.getCategory()), ServiceProject::getCategory, dto.getCategory())
                .eq(dto.getStatus() != null, ServiceProject::getStatus, dto.getStatus());

        IPage<ServiceProject> pageResult = serviceProjectService.page(page, wrapper);

        List<ServiceProjectVO> voList = new ArrayList<>();
        for (ServiceProject serviceProject : pageResult.getRecords()) {
            voList.add(toVO(serviceProject));
        }

        PageResult<ServiceProjectVO> result = new PageResult<>();
        result.setRecords(voList);
        result.setTotal(pageResult.getTotal());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取服务项目详情")
    public Result<ServiceProjectVO> get(@PathVariable Long id) {
        ServiceProject serviceProject = getExistingServiceProject(id);
        return Result.success(toVO(serviceProject));
    }

    @PostMapping
    @ApiOperation("新增服务项目")
    public Result<String> save(@Valid @RequestBody ServiceProjectDTO dto) {
        ServiceProject serviceProject = new ServiceProject();
        BeanUtils.copyProperties(dto, serviceProject);
        serviceProject.setCreateTime(LocalDateTime.now());
        serviceProject.setUpdateTime(LocalDateTime.now());

        boolean saved = serviceProjectService.save(serviceProject);
        if (!saved) {
            throw new BusinessException("新增服务项目失败");
        }
        return Result.success("新增服务项目成功");
    }

    @PutMapping("/{id}")
    @ApiOperation("修改服务项目")
    public Result<String> update(@PathVariable Long id, @Valid @RequestBody ServiceProjectDTO dto) {
        ServiceProject serviceProject = getExistingServiceProject(id);
        BeanUtils.copyProperties(dto, serviceProject);
        serviceProject.setId(id);
        serviceProject.setUpdateTime(LocalDateTime.now());

        boolean updated = serviceProjectService.updateById(serviceProject);
        if (!updated) {
            throw new BusinessException("修改服务项目失败");
        }
        return Result.success("修改服务项目成功");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除服务项目")
    public Result<String> delete(@PathVariable Long id) {
        getExistingServiceProject(id);

        boolean removed = serviceProjectService.removeById(id);
        if (!removed) {
            throw new BusinessException("删除服务项目失败");
        }
        return Result.success("删除服务项目成功");
    }

    @PatchMapping("/{id}/status")
    @ApiOperation("修改服务项目状态")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        ServiceProject serviceProject = getExistingServiceProject(id);
        serviceProject.setStatus(status);
        serviceProject.setUpdateTime(LocalDateTime.now());

        boolean updated = serviceProjectService.updateById(serviceProject);
        if (!updated) {
            throw new BusinessException("修改服务项目状态失败");
        }
        return Result.success("修改服务项目状态成功");
    }

    private ServiceProject getExistingServiceProject(Long id) {
        ServiceProject serviceProject = serviceProjectService.getById(id);
        if (serviceProject == null) {
            throw new BusinessException("服务项目不存在");
        }
        return serviceProject;
    }

    private ServiceProjectVO toVO(ServiceProject serviceProject) {
        ServiceProjectVO serviceProjectVO = new ServiceProjectVO();
        BeanUtils.copyProperties(serviceProject, serviceProjectVO);
        serviceProjectVO.setStatusName(CommonStatusEnum.labelOf(serviceProject.getStatus()));
        return serviceProjectVO;
    }
}
