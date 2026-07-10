package com.wkr.storeserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.ServiceProjectInventoryDTO;
import com.wkr.storepojo.dto.ServiceProjectInventoryPageQueryDTO;
import com.wkr.storepojo.entity.InventorySku;
import com.wkr.storepojo.entity.ServiceProject;
import com.wkr.storepojo.entity.ServiceProjectInventory;
import com.wkr.storepojo.enums.CommonStatusEnum;
import com.wkr.storepojo.vo.ServiceProjectInventoryVO;
import com.wkr.storeserver.service.InventorySkuService;
import com.wkr.storeserver.service.ServiceProjectInventoryService;
import com.wkr.storeserver.service.ServiceProjectService;
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
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务项目耗材关系接口控制器，负责维护项目完成时自动扣减的库存 SKU 配置。
 */
@Slf4j
@RestController
@RequestMapping("/admin/service-project-inventories")
@Api(tags = "服务项目耗材关系接口")
public class ServiceProjectInventoryController {

    private final ServiceProjectInventoryService serviceProjectInventoryService;
    private final ServiceProjectService serviceProjectService;
    private final InventorySkuService inventorySkuService;

    public ServiceProjectInventoryController(
            ServiceProjectInventoryService serviceProjectInventoryService,
            ServiceProjectService serviceProjectService,
            InventorySkuService inventorySkuService) {
        this.serviceProjectInventoryService = serviceProjectInventoryService;
        this.serviceProjectService = serviceProjectService;
        this.inventorySkuService = inventorySkuService;
    }

    @GetMapping
    @ApiOperation("分页查询服务项目耗材关系")
    public Result<PageResult<ServiceProjectInventoryVO>> list(ServiceProjectInventoryPageQueryDTO dto) {
        Page<ServiceProjectInventory> page = new Page<>(dto.getPage(), dto.getPageSize());
        LambdaQueryWrapper<ServiceProjectInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getServiceProjectId() != null,
                        ServiceProjectInventory::getServiceProjectId,
                        dto.getServiceProjectId())
                .eq(dto.getInventoryId() != null,
                        ServiceProjectInventory::getInventoryId,
                        dto.getInventoryId())
                .eq(dto.getStatus() != null,
                        ServiceProjectInventory::getStatus,
                        dto.getStatus())
                .orderByDesc(ServiceProjectInventory::getUpdateTime);

        IPage<ServiceProjectInventory> pageResult = serviceProjectInventoryService.page(page, wrapper);

        List<ServiceProjectInventoryVO> list = new ArrayList<>();
        for (ServiceProjectInventory item : pageResult.getRecords()) {
            list.add(toVO(item));
        }

        PageResult<ServiceProjectInventoryVO> result = new PageResult<>();
        result.setRecords(list);
        result.setTotal(pageResult.getTotal());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取服务项目耗材关系详情")
    public Result<ServiceProjectInventoryVO> get(@PathVariable("id") Long id) {
        return Result.success(toVO(getExistingRelation(id)));
    }

    @PostMapping
    @ApiOperation("新增服务项目耗材关系")
    public Result<Long> save(@Valid @RequestBody ServiceProjectInventoryDTO dto) {
        validateForeignKeys(dto);
        validateUnique(dto.getServiceProjectId(), dto.getInventoryId(), null);

        ServiceProjectInventory relation = new ServiceProjectInventory();
        BeanUtils.copyProperties(dto, relation);
        relation.setCreateTime(LocalDateTime.now());
        relation.setUpdateTime(LocalDateTime.now());

        boolean saved = serviceProjectInventoryService.save(relation);
        if (!saved) {
            throw new BusinessException("新增服务项目耗材关系失败");
        }
        return Result.success(relation.getId());
    }

    @PutMapping("/{id}")
    @ApiOperation("修改服务项目耗材关系")
    public Result<Boolean> update(@PathVariable("id") Long id, @Valid @RequestBody ServiceProjectInventoryDTO dto) {
        getExistingRelation(id);
        validateForeignKeys(dto);
        validateUnique(dto.getServiceProjectId(), dto.getInventoryId(), id);

        ServiceProjectInventory relation = new ServiceProjectInventory();
        BeanUtils.copyProperties(dto, relation);
        relation.setId(id);
        relation.setUpdateTime(LocalDateTime.now());

        return Result.success(serviceProjectInventoryService.updateById(relation));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除服务项目耗材关系")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        getExistingRelation(id);
        return Result.success(serviceProjectInventoryService.removeById(id));
    }

    private ServiceProjectInventory getExistingRelation(Long id) {
        ServiceProjectInventory relation = serviceProjectInventoryService.getById(id);
        if (relation == null) {
            throw new BusinessException("服务项目耗材关系不存在");
        }
        return relation;
    }

    private void validateForeignKeys(ServiceProjectInventoryDTO dto) {
        if (serviceProjectService.getById(dto.getServiceProjectId()) == null) {
            throw new BusinessException("服务项目不存在");
        }
        if (inventorySkuService.getById(dto.getInventoryId()) == null) {
            throw new BusinessException("库存物品不存在");
        }
    }

    private void validateUnique(Long serviceProjectId, Long inventoryId, Long excludeId) {
        LambdaQueryWrapper<ServiceProjectInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceProjectInventory::getServiceProjectId, serviceProjectId)
                .eq(ServiceProjectInventory::getInventoryId, inventoryId)
                .ne(excludeId != null, ServiceProjectInventory::getId, excludeId);
        if (serviceProjectInventoryService.count(wrapper) > 0) {
            throw new BusinessException("该服务项目已配置相同库存物品");
        }
    }

    private ServiceProjectInventoryVO toVO(ServiceProjectInventory relation) {
        ServiceProjectInventoryVO vo = new ServiceProjectInventoryVO();
        BeanUtils.copyProperties(relation, vo);
        vo.setStatusName(CommonStatusEnum.labelOf(relation.getStatus()));

        ServiceProject serviceProject = serviceProjectService.getById(relation.getServiceProjectId());
        if (serviceProject != null) {
            vo.setServiceProjectName(serviceProject.getName());
        }

        InventorySku inventorySku = inventorySkuService.getById(relation.getInventoryId());
        if (inventorySku != null) {
            vo.setInventoryName(inventorySku.getName());
            vo.setInventoryUnit(inventorySku.getUnit());
        }
        return vo;
    }
}
