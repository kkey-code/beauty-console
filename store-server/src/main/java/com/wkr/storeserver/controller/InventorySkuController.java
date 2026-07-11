package com.wkr.storeserver.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
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
import com.wkr.storeserver.audit.AuditLog;
import com.wkr.storeserver.service.DeletionGuardService;
import com.wkr.storeserver.excel.InventorySkuExcelVO;
import com.wkr.storeserver.service.InventorySkuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 库存物品接口控制器，负责接收管理端请求、校验参数并调用服务层完成业务处理。
 */
@Slf4j
@RestController
@RequestMapping("/admin/inventory-skus")
@Api(tags = "库存物品相关接口")
public class InventorySkuController {

    private static final long EXPORT_MAX_ROWS = 10_000L;
    private static final int EXPORT_PAGE_SIZE = 500;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final InventorySkuService inventorySkuService;
    private final DeletionGuardService deletionGuardService;

    public InventorySkuController(
            InventorySkuService inventorySkuService,
            DeletionGuardService deletionGuardService) {
        this.inventorySkuService = inventorySkuService;
        this.deletionGuardService = deletionGuardService;
    }

    @GetMapping
    @ApiOperation("分页查询库存物品")
    public Result<PageResult<InventorySkuVO>> queryInventorySkus(InventorySkuPageQueryDTO dto) {
        Page<InventorySku> page = new Page<>(dto.getPage(), dto.getPageSize());

        Page<InventorySku> pageResult = inventorySkuService.page(page, buildQueryWrapper(dto));

        List<InventorySkuVO> list = new ArrayList<>();
        for (InventorySku inventorySku : pageResult.getRecords()) {
            list.add(toVO(inventorySku));
        }

        PageResult<InventorySkuVO> result = new PageResult<>();
        result.setRecords(list);
        result.setTotal(pageResult.getTotal());
        return Result.success(result);
    }

    @GetMapping("/export")
    @ApiOperation("导出库存物品 Excel")
    public void export(InventorySkuPageQueryDTO dto, HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<InventorySku> wrapper = buildQueryWrapper(dto);
        long total = inventorySkuService.count(wrapper);
        if (total > EXPORT_MAX_ROWS) {
            throw new BusinessException("导出数据超过10000条，请缩小筛选条件");
        }

        String fileName = URLEncoder.encode("库存物品", StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), InventorySkuExcelVO.class).build()) {
            WriteSheet sheet = EasyExcel.writerSheet("库存物品").build();
            long pages = Math.max(1, (total + EXPORT_PAGE_SIZE - 1) / EXPORT_PAGE_SIZE);
            for (long pageNo = 1; pageNo <= pages; pageNo++) {
                Page<InventorySku> page = inventorySkuService.page(
                        new Page<>(pageNo, EXPORT_PAGE_SIZE, false),
                        wrapper);
                List<InventorySkuExcelVO> rows = page.getRecords().stream()
                        .map(this::toExcelVO)
                        .toList();
                excelWriter.write(rows, sheet);
            }
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据 id 查询库存物品")
    public Result<InventorySkuVO> getById(@PathVariable("id") Long id) {
        InventorySku inventorySku = getExistingInventorySku(id);
        return Result.success(toVO(inventorySku));
    }

    @PostMapping
    @ApiOperation("新增库存物品")
    @AuditLog(action = "CREATE", target = "INVENTORY_SKU")
    public Result<Boolean> add(@Valid @RequestBody InventorySkuDTO dto) {
        InventorySku inventorySku = new InventorySku();
        BeanUtils.copyProperties(dto, inventorySku);
        inventorySku.setCreateTime(LocalDateTime.now());
        inventorySku.setUpdateTime(LocalDateTime.now());

        return Result.success(inventorySkuService.save(inventorySku));
    }

    @PutMapping("/{id}")
    @ApiOperation("修改库存物品")
    @AuditLog(action = "UPDATE", target = "INVENTORY_SKU")
    public Result<Boolean> update(@PathVariable("id") Long id, @Valid @RequestBody InventorySkuDTO dto) {
        InventorySku inventorySku = getExistingInventorySku(id);
        BeanUtils.copyProperties(dto, inventorySku);
        inventorySku.setId(id);
        inventorySku.setUpdateTime(LocalDateTime.now());

        return Result.success(inventorySkuService.updateById(inventorySku));
    }

    @PatchMapping("/{id}/status")
    @ApiOperation("修改状态")
    @AuditLog(action = "STATUS", target = "INVENTORY_SKU")
    public Result<?> updateStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        InventorySku inventorySku = getExistingInventorySku(id);
        inventorySku.setStatus(status);
        inventorySku.setUpdateTime(LocalDateTime.now());

        boolean updated = inventorySkuService.updateById(inventorySku);
        return updated ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除库存物品")
    @AuditLog(action = "DELETE", target = "INVENTORY_SKU")
    public Result<?> delete(@PathVariable("id") Long id) {
        getExistingInventorySku(id);
        deletionGuardService.assertInventorySkuCanDelete(id);
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

    private LambdaQueryWrapper<InventorySku> buildQueryWrapper(InventorySkuPageQueryDTO dto) {
        LambdaQueryWrapper<InventorySku> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getName()), InventorySku::getName, dto.getName())
                .eq(StringUtils.hasText(dto.getCategory()), InventorySku::getCategory, dto.getCategory())
                .eq(dto.getStatus() != null, InventorySku::getStatus, dto.getStatus())
                .apply(Boolean.TRUE.equals(dto.getLowStockOnly()), "quantity <= safety_stock");
        if (Boolean.TRUE.equals(dto.getLowStockOnly())) {
            wrapper.orderByAsc(InventorySku::getQuantity)
                    .orderByAsc(InventorySku::getId);
        } else {
            wrapper.orderByDesc(InventorySku::getCreateTime)
                    .orderByDesc(InventorySku::getId);
        }
        return wrapper;
    }

    private InventorySkuVO toVO(InventorySku inventorySku) {
        InventorySkuVO inventorySkuVO = new InventorySkuVO();
        BeanUtils.copyProperties(inventorySku, inventorySkuVO);
        inventorySkuVO.setStatusName(CommonStatusEnum.labelOf(inventorySku.getStatus()));
        inventorySkuVO.setBelowSafetyStock(isBelowSafetyStock(inventorySku));
        return inventorySkuVO;
    }

    private InventorySkuExcelVO toExcelVO(InventorySku inventorySku) {
        InventorySkuExcelVO excelVO = new InventorySkuExcelVO();
        excelVO.setId(inventorySku.getId());
        excelVO.setName(inventorySku.getName());
        excelVO.setCategory(inventorySku.getCategory());
        excelVO.setUnit(inventorySku.getUnit());
        excelVO.setQuantity(inventorySku.getQuantity());
        excelVO.setSafetyStock(inventorySku.getSafetyStock());
        excelVO.setBelowSafetyStock(isBelowSafetyStock(inventorySku) ? "是" : "否");
        excelVO.setCostPrice(inventorySku.getCostPrice());
        excelVO.setSupplier(inventorySku.getSupplier());
        excelVO.setStatusName(CommonStatusEnum.labelOf(inventorySku.getStatus()));
        excelVO.setRemark(inventorySku.getRemark());
        excelVO.setCreateTime(formatDateTime(inventorySku.getCreateTime()));
        excelVO.setUpdateTime(formatDateTime(inventorySku.getUpdateTime()));
        return excelVO;
    }

    private boolean isBelowSafetyStock(InventorySku inventorySku) {
        BigDecimal quantity = inventorySku.getQuantity();
        BigDecimal safetyStock = inventorySku.getSafetyStock();
        return quantity != null && safetyStock != null && quantity.compareTo(safetyStock) < 0;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMATTER);
    }
}
