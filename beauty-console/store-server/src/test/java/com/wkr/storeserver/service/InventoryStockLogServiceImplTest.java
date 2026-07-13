package com.wkr.storeserver.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.InventoryStockLogDTO;
import com.wkr.storepojo.entity.InventorySku;
import com.wkr.storepojo.entity.InventoryStockLog;
import com.wkr.storepojo.enums.InventoryChangeTypeEnum;
import com.wkr.storeserver.mapper.InventorySkuMapper;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryStockLogServiceImplTest {

    @Mock
    private InventoryStockLogMapper inventoryStockLogMapper;

    @Mock
    private InventorySkuService inventorySkuService;

    @Mock
    private InventorySkuMapper inventorySkuMapper;

    private InventoryStockLogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InventoryStockLogServiceImpl(inventoryStockLogMapper, inventorySkuService, inventorySkuMapper);
    }

    @AfterEach
    void tearDown() {
        BaseContext.remove();
    }

    @Test
    void stockOutUpdatesSkuAndWritesStockLog() {
        when(inventorySkuService.getOne(any(Wrapper.class))).thenReturn(inventorySku(BigDecimal.TEN));
        when(inventorySkuMapper.updateQuantityIfUnchanged(anyLong(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(1);
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
        verify(inventorySkuMapper).updateQuantityIfUnchanged(
                eq(1001L),
                eq(BigDecimal.TEN),
                eq(BigDecimal.valueOf(7)));
    }

    @Test
    void currentStaffContextOverridesSubmittedOperatorId() {
        BaseContext.setCurrentUser(9001L, 9002L, 4, "INVENTORY_ADMIN");
        when(inventorySkuService.getOne(any(Wrapper.class))).thenReturn(inventorySku(BigDecimal.TEN));
        when(inventorySkuMapper.updateQuantityIfUnchanged(anyLong(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(1);
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
        verify(inventorySkuMapper, never()).updateQuantityIfUnchanged(
                anyLong(),
                any(BigDecimal.class),
                any(BigDecimal.class));
        verify(inventoryStockLogMapper, never()).insert(any(InventoryStockLog.class));
    }

    @Test
    void concurrentQuantityChangeRetriesWithLatestStock() {
        when(inventorySkuService.getOne(any(Wrapper.class)))
                .thenReturn(inventorySku(BigDecimal.TEN), inventorySku(BigDecimal.valueOf(9)));
        when(inventorySkuMapper.updateQuantityIfUnchanged(anyLong(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(0, 1);
        when(inventoryStockLogMapper.insert(any(InventoryStockLog.class))).thenReturn(1);

        InventoryStockLog result = service.recordStockChange(
                stockLogDto(BigDecimal.ONE),
                InventoryChangeTypeEnum.STOCK_OUT.getCode());

        assertEquals(0, BigDecimal.valueOf(9).compareTo(result.getBeforeQuantity()));
        assertEquals(0, BigDecimal.valueOf(8).compareTo(result.getAfterQuantity()));
        verify(inventorySkuService, times(2)).getOne(any(Wrapper.class));
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
