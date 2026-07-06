package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storepojo.entity.ServiceProject;
import com.wkr.storeserver.service.ServiceProjectService;
import com.wkr.storeserver.mapper.ServiceProjectMapper;
import org.springframework.stereotype.Service;

/**
* @author kkey
* @description 针对表【service_project(服务项目表)】的数据库操作Service实现
* @createDate 2026-07-03 21:10:41
*/
@Service
public class ServiceProjectServiceImpl extends ServiceImpl<ServiceProjectMapper, ServiceProject>
    implements ServiceProjectService{

}




