package com.wkr.storeserver.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wkr.storepojo.entity.CustomerProfile;

/**
* @author kkey
* @description 针对表【customer_profile(客户档案表)】的数据库操作Service
* @createDate 2026-07-03 21:29:31
*/
public interface CustomerProfileService extends IService<CustomerProfile> {

    IPage<CustomerProfile> pageVisible(Page<CustomerProfile> page, LambdaQueryWrapper<CustomerProfile> wrapper);

    CustomerProfile getVisibleById(Long id);

    long countVisibleCustomers();

    void assertCanAccess(Long id);
}
