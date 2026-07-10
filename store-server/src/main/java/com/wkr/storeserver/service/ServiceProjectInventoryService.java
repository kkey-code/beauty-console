package com.wkr.storeserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkr.storepojo.entity.ServiceProjectInventory;

import java.util.Collection;
import java.util.List;

/**
 * 服务项目耗材关系服务，提供项目关联库存 SKU 的配置查询能力。
 */
public interface ServiceProjectInventoryService extends IService<ServiceProjectInventory> {

    List<ServiceProjectInventory> listActiveByProjectIds(Collection<Long> serviceProjectIds);
}
