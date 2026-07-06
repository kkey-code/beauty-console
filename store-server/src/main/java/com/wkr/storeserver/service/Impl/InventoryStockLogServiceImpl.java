package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storepojo.entity.InventoryStockLog;
import com.wkr.storeserver.service.InventoryStockLogService;
import com.wkr.storeserver.mapper.InventoryStockLogMapper;
import org.springframework.stereotype.Service;

/**
* @author kkey
* @description 针对表【inventory_stock_log(库存流水表)】的数据库操作Service实现
* @createDate 2026-07-03 21:10:41
*/
@Service
public class InventoryStockLogServiceImpl extends ServiceImpl<InventoryStockLogMapper, InventoryStockLog>
    implements InventoryStockLogService{

}




