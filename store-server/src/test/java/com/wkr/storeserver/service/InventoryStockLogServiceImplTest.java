package com.wkr.storeserver.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.InventoryStockLogDTO;
import com.wkr.storepojo.entity.InventorySku;
import com.wkr.storepojo.entity.InventoryStockLog;
import com.wkr.storepojo.enums.InventoryChangeTypeEnum;
import com.wkr.storeserver.mapper.InventoryStockLogMapper;
import com.wkr.storeserver.service.impl.InventoryStockLogServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 库存流水服务测试，验证出入库会同步更新 SKU 库存并记录变动前后数量。
 */
@ExtendWith(MockitoExtension.class)
class InventoryStockLogServiceImplTest {

    @Mock
    private InventoryStockLogMapper inventoryStockLogMapper;

    @Mock
    private InventorySkuService inventorySkuService;

    private InventoryStockLogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InventoryStockLogServiceImpl(inventoryStockLogMapper, inventorySkuService);
    }

    @AfterEach
    void tearDown() {
        BaseContext.remove();
    }

    @Test
    void stockOutUpdatesSkuAndWritesStockLog() {
        InventorySku inventorySku = inventorySku(BigDecimal.TEN);
        when(inventorySkuService.getOne(any(Wrapper.class))).thenReturn(inventorySku);
        when(inventorySkuService.updateById(any(InventorySku.class))).thenReturn(true);
        when(inventoryStockLogMapper.insert(any(InventoryStockLog.class))).thenAnswer(invocation -> {
            InventoryStockLog log = invocation.getArgument(0);
            log.setId(1L);
            return 1;
        });

        InventoryStockLog result = service.recordStockChange(
                stockLogDto(BigDecimal.valueOf(3)),
                InventoryChangeTypeEnum.STOCK_OUT.getCode());

        assertEquals(1L, result.getId());
        assertEquals(0, BigDecimal.TEN.compareTo(result.getBeforeQuantity()));
        assertEquals(0, BigDecimal.valueOf(7).compareTo(result.getAfterQuantity()));

        ArgumentCaptor<InventorySku> skuCaptor = ArgumentCaptor.forClass(InventorySku.class);
        verify(inventorySkuService).updateById(skuCaptor.capture());
        assertEquals(0, BigDecimal.valueOf(7).compareTo(skuCaptor.getValue().getQuantity()));
    }

    @Test
    void currentStaffContextOverridesSubmittedOperatorId() {
        BaseContext.setCurrentUser(9001L, 9002L, 4, "INVENTORY_ADMIN");
        when(inventorySkuService.getOne(any(Wrapper.class))).thenReturn(inventorySku(BigDecimal.TEN));
        when(inventorySkuService.updateById(any(InventorySku.class))).thenReturn(true);
        when(inventoryStockLogMapper.insert(any(InventoryStockLog.class))).thenReturn(1);

        service.recordStockChange(stockLogDto(BigDecimal.ONE), InventoryChangeTypeEnum.STOCK_OUT.getCode());

        ArgumentCaptor<InventoryStockLog> logCaptor = ArgumentCaptor.forClass(InventoryStockLog.class);
        verify(inventoryStockLogMapper).insert(logCaptor.capture());
        assertEquals(9002L, logCaptor.getValue().getOperatorId());
    }

    @Test
    void insufficientStockRejectsStockOut() {
        when(inventorySkuService.getOne(any(Wrapper.class))).thenReturn(inventorySku(BigDecimal.ONE));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.recordStockChange(
                        stockLogDto(BigDecimal.valueOf(3)),
                        InventoryChangeTypeEnum.STOCK_OUT.getCode()));

        assertTrue(exception.getMessage().contains("库存不足"));
        verify(inventorySkuService, never()).updateById(any(InventorySku.class));
        verify(inventoryStockLogMapper, never()).insert(any(InventoryStockLog.class));
    }

    private InventorySku inventorySku(BigDecimal quantity) {
        InventorySku inventorySku = new InventorySku();
        inventorySku.setId(1001L);
        inventorySku.setQuantity(quantity);
        return inventorySku;
    }

    private InventoryStockLogDTO stockLogDto(BigDecimal changeQuantity) {
        InventoryStockLogDTO dto = new InventoryStockLogDTO();
        dto.setInventoryId(1001L);
        dto.setChangeType(InventoryChangeTypeEnum.STOCK_OUT.getCode());
        dto.setChangeQuantity(changeQuantity);
        dto.setRelatedOrderId(2001L);
        dto.setOperatorId(3001L);
        return dto;
    }
}
