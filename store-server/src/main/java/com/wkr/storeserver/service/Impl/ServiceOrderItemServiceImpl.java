package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storepojo.entity.ServiceOrderItem;
import com.wkr.storeserver.service.ServiceOrderItemService;
import com.wkr.storeserver.mapper.ServiceOrderItemMapper;
import org.springframework.stereotype.Service;

/**
* @author kkey
* @description 针对表【service_order_item(订单项目明细表)】的数据库操作Service实现
* @createDate 2026-07-03 21:10:41
*/
@Service
public class ServiceOrderItemServiceImpl extends ServiceImpl<ServiceOrderItemMapper, ServiceOrderItem>
    implements ServiceOrderItemService{

}




