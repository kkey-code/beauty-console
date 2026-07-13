package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storepojo.entity.ServiceProjectInventory;
import com.wkr.storeserver.mapper.ServiceProjectInventoryMapper;
import com.wkr.storeserver.service.ServiceProjectInventoryService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务项目耗材关系服务实现，封装启用关系的查询规则。
 */
@Service
public class ServiceProjectInventoryServiceImpl
        extends ServiceImpl<ServiceProjectInventoryMapper, ServiceProjectInventory>
        implements ServiceProjectInventoryService {

    @Override
    @Cacheable(
            cacheNames = "serviceProjectInventory:active",
            key = "#root.target.projectIdsCacheKey(#serviceProjectIds)",
            unless = "#result == null || #result.isEmpty()")
    public List<ServiceProjectInventory> listActiveByProjectIds(Collection<Long> serviceProjectIds) {
        if (serviceProjectIds == null || serviceProjectIds.isEmpty()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<ServiceProjectInventory>()
                .in(ServiceProjectInventory::getServiceProjectId, serviceProjectIds)
                .eq(ServiceProjectInventory::getStatus, 1));
    }

    @Override
    @CacheEvict(cacheNames = "serviceProjectInventory:active", allEntries = true)
    public boolean save(ServiceProjectInventory entity) {
        return super.save(entity);
    }

    @Override
    @CacheEvict(cacheNames = "serviceProjectInventory:active", allEntries = true)
    public boolean updateById(ServiceProjectInventory entity) {
        return super.updateById(entity);
    }

    @Override
    @CacheEvict(cacheNames = "serviceProjectInventory:active", allEntries = true)
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }

    public String projectIdsCacheKey(Collection<Long> serviceProjectIds) {
        if (serviceProjectIds == null || serviceProjectIds.isEmpty()) {
            return "empty";
        }
        return serviceProjectIds.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
