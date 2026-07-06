package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storepojo.entity.CustomerProfile;
import com.wkr.storeserver.service.CustomerProfileService;
import com.wkr.storeserver.mapper.CustomerProfileMapper;
import org.springframework.stereotype.Service;

/**
* @author kkey
* @description 针对表【customer_profile(客户档案表)】的数据库操作Service实现
* @createDate 2026-07-03 21:29:31
*/
@Service
public class CustomerProfileServiceImpl extends ServiceImpl<CustomerProfileMapper, CustomerProfile>
    implements CustomerProfileService{

}




