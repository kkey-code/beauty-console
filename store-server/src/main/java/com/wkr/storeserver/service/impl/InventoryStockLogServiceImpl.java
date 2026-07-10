package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.InventoryStockLogDTO;
import com.wkr.storepojo.entity.InventorySku;
import com.wkr.storepojo.entity.InventoryStockLog;
import com.wkr.storepojo.enums.InventoryChangeTypeEnum;
import com.wkr.storeserver.mapper.InventoryStockLogMapper;
import com.wkr.storeserver.service.InventorySkuService;
import com.wkr.storeserver.service.InventoryStockLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
* @author kkey
* @description 针对表【inventory_stock_log(库存流水表)】的数据库操作Service实现
* @createDate 2026-07-03 21:10:41
*/
@Service
public class InventoryStockLogServiceImpl extends ServiceImpl<InventoryStockLogMapper, InventoryStockLog>
    implements InventoryStockLogService{

    private final InventoryStockLogMapper inventoryStockLogMapper;
    private final InventorySkuService inventorySkuService;

    public InventoryStockLogServiceImpl(
            InventoryStockLogMapper inventoryStockLogMapper,
            InventorySkuService inventorySkuService) {
        this.inventoryStockLogMapper = inventoryStockLogMapper;
        this.inventorySkuService = inventorySkuService;
    }

    @Override
    @Transactional
    public InventoryStockLog recordStockChange(InventoryStockLogDTO dto, String changeType) {
        String normalizedChangeType = normalizeChangeType(changeType);
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

        InventoryStockLog inventoryStockLog = new InventoryStockLog();
        BeanUtils.copyProperties(dto, inventoryStockLog);
        Long currentStaffId = BaseContext.getCurrentId();
        if (currentStaffId != null) {
            inventoryStockLog.setOperatorId(currentStaffId);
        }
        inventoryStockLog.setChangeType(normalizedChangeType);
        inventoryStockLog.setBeforeQuantity(beforeQuantity);
        inventoryStockLog.setAfterQuantity(afterQuantity);
        inventoryStockLog.setCreateTime(LocalDateTime.now());

        inventorySku.setQuantity(afterQuantity);
        inventorySku.setUpdateTime(LocalDateTime.now());
        boolean updated = inventorySkuService.updateById(inventorySku);
        if (!updated) {
            throw new BusinessException("更新库存失败");
        }
        if (inventoryStockLogMapper.insert(inventoryStockLog) != 1) {
            throw new BusinessException("保存库存流水失败");
        }
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
