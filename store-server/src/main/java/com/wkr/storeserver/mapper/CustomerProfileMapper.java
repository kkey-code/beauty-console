package com.wkr.storeserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wkr.storepojo.entity.CustomerProfile;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author kkey
* @description 针对表【customer_profile(客户档案表)】的数据库操作Mapper
* @createDate 2026-07-03 21:29:31
* @Entity com.wkr.storepojo.entity.CustomerProfile
*/
public interface CustomerProfileMapper extends BaseMapper<CustomerProfile> {

}




