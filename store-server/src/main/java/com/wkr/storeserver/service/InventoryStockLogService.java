package com.wkr.storeserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkr.storepojo.dto.InventoryStockLogDTO;
import com.wkr.storepojo.entity.InventoryStockLog;

/**
* @author kkey
* @description 针对表【inventory_stock_log(库存流水表)】的数据库操作Service
* @createDate 2026-07-03 21:10:41
*/
public interface InventoryStockLogService extends IService<InventoryStockLog> {

    InventoryStockLog recordStockChange(InventoryStockLogDTO dto, String changeType);
}
