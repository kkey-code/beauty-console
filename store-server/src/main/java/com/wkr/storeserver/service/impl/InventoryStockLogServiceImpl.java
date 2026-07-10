package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.InventoryStockLogDTO;
import com.wkr.storepojo.entity.InventorySku;
import com.wkr.storepojo.entity.InventoryStockLog;
import com.wkr.storepojo.enums.InventoryChangeTypeEnum;
import com.wkr.storeserver.mapper.InventorySkuMapper;
import com.wkr.storeserver.mapper.InventoryStockLogMapper;
import com.wkr.storeserver.service.InventorySkuService;
import com.wkr.storeserver.service.InventoryStockLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class InventoryStockLogServiceImpl extends ServiceImpl<InventoryStockLogMapper, InventoryStockLog>
        implements InventoryStockLogService {

    private static final int MAX_STOCK_UPDATE_RETRY = 3;

    private final InventoryStockLogMapper inventoryStockLogMapper;
    private final InventorySkuService inventorySkuService;
    private final InventorySkuMapper inventorySkuMapper;

    public InventoryStockLogServiceImpl(
            InventoryStockLogMapper inventoryStockLogMapper,
            InventorySkuService inventorySkuService,
            InventorySkuMapper inventorySkuMapper) {
        this.inventoryStockLogMapper = inventoryStockLogMapper;
        this.inventorySkuService = inventorySkuService;
        this.inventorySkuMapper = inventorySkuMapper;
    }

    @Override
    @Transactional
    public InventoryStockLog recordStockChange(InventoryStockLogDTO dto, String changeType) {
        String normalizedChangeType = normalizeChangeType(changeType);

        for (int attempt = 1; attempt <= MAX_STOCK_UPDATE_RETRY; attempt++) {
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
            BigDecimal afterQuantity = calculateAfterQuantity(beforeQuantity, changeQuantity, normalizedChangeType);
            if (afterQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("库存不足，不能出库");
            }

            int updated = inventorySkuMapper.updateQuantityIfUnchanged(
                    inventorySku.getId(),
                    beforeQuantity,
                    afterQuantity);
            if (updated == 0) {
                continue;
            }
            if (updated != 1) {
                throw new BusinessException("更新库存失败");
            }

            InventoryStockLog inventoryStockLog = buildStockLog(
                    dto,
                    normalizedChangeType,
                    beforeQuantity,
                    afterQuantity);
            if (inventoryStockLogMapper.insert(inventoryStockLog) != 1) {
                throw new BusinessException("保存库存流水失败");
            }
            return inventoryStockLog;
        }

        throw new BusinessException("库存被并发修改，请重试");
    }

    private InventoryStockLog buildStockLog(
            InventoryStockLogDTO dto,
            String changeType,
            BigDecimal beforeQuantity,
            BigDecimal afterQuantity) {
        InventoryStockLog inventoryStockLog = new InventoryStockLog();
        BeanUtils.copyProperties(dto, inventoryStockLog);
        Long currentStaffId = BaseContext.getCurrentId();
        if (currentStaffId != null) {
            inventoryStockLog.setOperatorId(currentStaffId);
        }
        inventoryStockLog.setChangeType(changeType);
        inventoryStockLog.setBeforeQuantity(beforeQuantity);
        inventoryStockLog.setAfterQuantity(afterQuantity);
        inventoryStockLog.setCreateTime(LocalDateTime.now());
        return inventoryStockLog;
    }

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

    private String normalizeChangeType(String changeType) {
        for (InventoryChangeTypeEnum item : InventoryChangeTypeEnum.values()) {
            if (item.matches(changeType)) {
                return item.getCode();
            }
        }
        throw new BusinessException("库存变动类型错误");
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
