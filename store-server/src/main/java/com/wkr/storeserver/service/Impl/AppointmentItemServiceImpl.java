package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storepojo.entity.AppointmentItem;
import com.wkr.storeserver.service.AppointmentItemService;
import com.wkr.storeserver.mapper.AppointmentItemMapper;
import org.springframework.stereotype.Service;

/**
* @author kkey
* @description 针对表【appointment_item(预约项目明细表)】的数据库操作Service实现
* @createDate 2026-07-03 21:10:41
*/
@Service
public class AppointmentItemServiceImpl extends ServiceImpl<AppointmentItemMapper, AppointmentItem>
    implements AppointmentItemService{

}




