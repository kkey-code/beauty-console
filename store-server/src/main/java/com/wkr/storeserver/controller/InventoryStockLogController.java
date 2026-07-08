package com.wkr.storeserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.InventoryStockLogDTO;
import com.wkr.storepojo.dto.InventoryStockLogPageQueryDTO;
import com.wkr.storepojo.entity.InventorySku;
import com.wkr.storepojo.entity.InventoryStockLog;
import com.wkr.storepojo.enums.InventoryChangeTypeEnum;
import com.wkr.storepojo.vo.InventoryStockLogVO;
import com.wkr.storeserver.service.InventorySkuService;
import com.wkr.storeserver.service.InventoryStockLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/inventory-stock-logs")
@Api(tags = "库存流水相关接口")
public class InventoryStockLogController {

    private final InventoryStockLogService inventoryStockLogService;
    private final InventorySkuService inventorySkuService;

    public InventoryStockLogController(
            InventoryStockLogService inventoryStockLogService,
            InventorySkuService inventorySkuService) {
        this.inventoryStockLogService = inventoryStockLogService;
        this.inventorySkuService = inventorySkuService;
    }

    @GetMapping
    @ApiOperation("库存流水列表")
    public Result<PageResult<InventoryStockLogVO>> list(InventoryStockLogPageQueryDTO dto) {
        Page<InventoryStockLog> page = new Page<>(dto.getPage(), dto.getPageSize());
        String changeType = StringUtils.hasText(dto.getChangeType()) ? normalizeChangeType(dto.getChangeType()) : null;

        LambdaQueryWrapper<InventoryStockLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getInventoryId() != null, InventoryStockLog::getInventoryId, dto.getInventoryId())
                .eq(changeType != null, InventoryStockLog::getChangeType, changeType)
                .ge(dto.getBeginTime() != null, InventoryStockLog::getCreateTime, dto.getBeginTime())
                .le(dto.getEndTime() != null, InventoryStockLog::getCreateTime, dto.getEndTime())
                .eq(dto.getRelatedOrderId() != null, InventoryStockLog::getRelatedOrderId, dto.getRelatedOrderId());

        Page<InventoryStockLog> pageResult = inventoryStockLogService.page(page, wrapper);

        List<InventoryStockLogVO> list = new ArrayList<>();
        for (InventoryStockLog inventoryStockLog : pageResult.getRecords()) {
            list.add(toVO(inventoryStockLog));
        }
        return Result.success(PageResult.<InventoryStockLogVO>builder()
                .total(pageResult.getTotal())
                .records(list)
                .build());
    }

    @GetMapping("/{id}")
    @ApiOperation("查询库存流水")
    public Result<InventoryStockLogVO> get(@PathVariable("id") Long id) {
        InventoryStockLog inventoryStockLog = inventoryStockLogService.getById(id);
        if (inventoryStockLog == null) {
            throw new BusinessException("库存流水不存在");
        }
        return Result.success(toVO(inventoryStockLog));
    }

    @PostMapping
    @ApiOperation("新增库存流水")
    @Transactional
    public Result<Boolean> add(@Valid @RequestBody InventoryStockLogDTO dto) {
        saveStockLog(dto, normalizeChangeType(dto.getChangeType()));
        return Result.success(true);
    }

    @PostMapping("/inbound")
    @ApiOperation("入库")
    @Transactional
    public Result<Long> inbound(@Valid @RequestBody InventoryStockLogDTO dto) {
        InventoryStockLog inventoryStockLog = saveStockLog(dto, InventoryChangeTypeEnum.STOCK_IN.getCode());
        return Result.success(inventoryStockLog.getId());
    }

    @PostMapping("/outbound")
    @ApiOperation("出库")
    @Transactional
    public Result<Long> outbound(@Valid @RequestBody InventoryStockLogDTO dto) {
        InventoryStockLog inventoryStockLog = saveStockLog(dto, InventoryChangeTypeEnum.STOCK_OUT.getCode());
        return Result.success(inventoryStockLog.getId());
    }

    @PostMapping("/adjust")
    @ApiOperation("盘点调整")
    @Transactional
    public Result<Long> adjust(@Valid @RequestBody InventoryStockLogDTO dto) {
        InventoryStockLog inventoryStockLog = saveStockLog(dto, InventoryChangeTypeEnum.CHECK.getCode());
        return Result.success(inventoryStockLog.getId());
    }
    // 保存库存流水并更新库存(盘点调整)
    private InventoryStockLog saveStockLog(InventoryStockLogDTO dto, String changeType) {
        //加悲观锁
        InventorySku inventorySku = inventorySkuService.getOne(
                new LambdaQueryWrapper<InventorySku>()
                        .eq(InventorySku::getId, dto.getInventoryId())
                        .last("FOR UPDATE")
        );
        if (inventorySku == null) {
            throw new BusinessException("库存物品不存在");
        }

        BigDecimal beforeQuantity = valueOrZero(inventorySku.getQuantity());
        BigDecimal changeQuantity = valueOrZero(dto.getChangeQuantity());
        BigDecimal afterQuantity = calculateAfterQuantity(beforeQuantity, changeQuantity, changeType);
        if (afterQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("库存不足，不能出库");
        }

        InventoryStockLog inventoryStockLog = new InventoryStockLog();
        BeanUtils.copyProperties(dto, inventoryStockLog);
        inventoryStockLog.setChangeType(changeType);
        inventoryStockLog.setBeforeQuantity(beforeQuantity);
        inventoryStockLog.setAfterQuantity(afterQuantity);
        inventoryStockLog.setCreateTime(LocalDateTime.now());

        inventorySku.setQuantity(afterQuantity);
        inventorySku.setUpdateTime(LocalDateTime.now());
        inventorySkuService.updateById(inventorySku);
        inventoryStockLogService.save(inventoryStockLog);
        return inventoryStockLog;
    }
    // 计算库存变动后的数量
    private BigDecimal calculateAfterQuantity(BigDecimal beforeQuantity, BigDecimal changeQuantity, String changeType) {
        if (InventoryChangeTypeEnum.STOCK_IN.matches(changeType)
                || InventoryChangeTypeEnum.RETURN.matches(changeType)) {
            return beforeQuantity.add(changeQuantity);
        }
        if (InventoryChangeTypeEnum.STOCK_OUT.matches(changeType)
                || InventoryChangeTypeEnum.LOSS.matches(changeType)) {
            return beforeQuantity.subtract(changeQuantity);
        }
        if (InventoryChangeTypeEnum.CHECK.matches(changeType)) {
            return changeQuantity;
        }
        throw new BusinessException("库存变动类型错误");
    }
    // 处理库存变动类型
    private String normalizeChangeType(String changeType) {
        for (InventoryChangeTypeEnum item : InventoryChangeTypeEnum.values()) {
            if (item.matches(changeType)) {
                return item.getCode();
            }
        }
        throw new BusinessException("库存变动类型错误");
    }
    // 转换为VO
    private InventoryStockLogVO toVO(InventoryStockLog inventoryStockLog) {
        InventoryStockLogVO vo = new InventoryStockLogVO();
        BeanUtils.copyProperties(inventoryStockLog, vo);
        vo.setChangeTypeName(InventoryChangeTypeEnum.labelOf(inventoryStockLog.getChangeType()));

        InventorySku inventorySku = inventorySkuService.getById(inventoryStockLog.getInventoryId());
        if (inventorySku != null) {
            vo.setInventoryName(inventorySku.getName());
        }
        return vo;
    }
    // 处理空值
    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
