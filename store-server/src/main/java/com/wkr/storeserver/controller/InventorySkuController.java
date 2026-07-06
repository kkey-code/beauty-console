package com.wkr.storeserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.InventorySkuDTO;
import com.wkr.storepojo.dto.InventorySkuPageQueryDTO;
import com.wkr.storepojo.entity.InventorySku;
import com.wkr.storepojo.enums.CommonStatusEnum;
import com.wkr.storepojo.vo.InventorySkuVO;
import com.wkr.storeserver.service.InventorySkuService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/inventory-skus")
@Api(tags = "库存物品相关接口")
public class InventorySkuController {

    private final InventorySkuService inventorySkuService;

    public InventorySkuController(InventorySkuService inventorySkuService) {
        this.inventorySkuService = inventorySkuService;
    }

    @GetMapping
    @ApiOperation("分页查询库存物品")
    public Result<PageResult<InventorySkuVO>> queryInventorySkus(InventorySkuPageQueryDTO dto) {
        Page<InventorySku> page = new Page<>(dto.getPage(), dto.getPageSize());

        LambdaQueryWrapper<InventorySku> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getName()), InventorySku::getName, dto.getName())
                .eq(StringUtils.hasText(dto.getCategory()), InventorySku::getCategory, dto.getCategory())
                .eq(dto.getStatus() != null, InventorySku::getStatus, dto.getStatus())
                .apply(Boolean.TRUE.equals(dto.getLowStockOnly()), "quantity < safety_stock");

        Page<InventorySku> pageResult = inventorySkuService.page(page, wrapper);

        List<InventorySkuVO> list = new ArrayList<>();
        for (InventorySku inventorySku : pageResult.getRecords()) {
            list.add(toVO(inventorySku));
        }

        PageResult<InventorySkuVO> result = new PageResult<>();
        result.setRecords(list);
        result.setTotal(pageResult.getTotal());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据 id 查询库存物品")
    public Result<InventorySkuVO> getById(@PathVariable Long id) {
        InventorySku inventorySku = getExistingInventorySku(id);
        return Result.success(toVO(inventorySku));
    }

    @PostMapping
    @ApiOperation("新增库存物品")
    public Result<Boolean> add(@Valid @RequestBody InventorySkuDTO dto) {
        InventorySku inventorySku = new InventorySku();
        BeanUtils.copyProperties(dto, inventorySku);
        inventorySku.setCreateTime(LocalDateTime.now());
        inventorySku.setUpdateTime(LocalDateTime.now());

        return Result.success(inventorySkuService.save(inventorySku));
    }

    @PutMapping("/{id}")
    @ApiOperation("修改库存物品")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody InventorySkuDTO dto) {
        InventorySku inventorySku = getExistingInventorySku(id);
        BeanUtils.copyProperties(dto, inventorySku);
        inventorySku.setId(id);
        inventorySku.setUpdateTime(LocalDateTime.now());

        return Result.success(inventorySkuService.updateById(inventorySku));
    }

    @PatchMapping("/{id}/status")
    @ApiOperation("修改状态")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        InventorySku inventorySku = getExistingInventorySku(id);
        inventorySku.setStatus(status);
        inventorySku.setUpdateTime(LocalDateTime.now());

        boolean updated = inventorySkuService.updateById(inventorySku);
        return updated ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除库存物品")
    public Result<?> delete(@PathVariable Long id) {
        getExistingInventorySku(id);
        boolean removed = inventorySkuService.removeById(id);
        return removed ? Result.success() : Result.error("删除失败");
    }

    private InventorySku getExistingInventorySku(Long id) {
        InventorySku inventorySku = inventorySkuService.getById(id);
        if (inventorySku == null) {
            throw new BusinessException("库存物品不存在");
        }
        return inventorySku;
    }

    private InventorySkuVO toVO(InventorySku inventorySku) {
        InventorySkuVO inventorySkuVO = new InventorySkuVO();
        BeanUtils.copyProperties(inventorySku, inventorySkuVO);
        inventorySkuVO.setStatusName(CommonStatusEnum.labelOf(inventorySku.getStatus()));
        inventorySkuVO.setBelowSafetyStock(isBelowSafetyStock(inventorySku));
        return inventorySkuVO;
    }

    private boolean isBelowSafetyStock(InventorySku inventorySku) {
        BigDecimal quantity = inventorySku.getQuantity();
        BigDecimal safetyStock = inventorySku.getSafetyStock();
        return quantity != null && safetyStock != null && quantity.compareTo(safetyStock) < 0;
    }
}
