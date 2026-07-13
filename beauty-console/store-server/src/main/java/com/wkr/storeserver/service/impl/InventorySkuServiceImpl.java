package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storepojo.entity.InventorySku;
import com.wkr.storeserver.service.InventorySkuService;
import com.wkr.storeserver.mapper.InventorySkuMapper;
import org.springframework.stereotype.Service;

/**
* @author kkey
* @description 针对表【inventory_sku(库存物品表)】的数据库操作Service实现
* @createDate 2026-07-03 21:10:41
*/
@Service
public class InventorySkuServiceImpl extends ServiceImpl<InventorySkuMapper, InventorySku>
    implements InventorySkuService{

}




