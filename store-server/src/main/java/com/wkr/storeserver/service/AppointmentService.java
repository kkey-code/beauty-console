package com.wkr.storeserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storepojo.dto.AppointmentPageQueryDTO;
import com.wkr.storepojo.entity.Appointment;
import com.wkr.storepojo.vo.AppointmentVO;

/**
* @author kkey
* @description 针对表【appointment(预约主表)】的数据库操作Service
* @createDate 2026-07-03 21:10:41
*/
public interface AppointmentService extends IService<Appointment> {

    Result<PageResult<AppointmentVO>> List(AppointmentPageQueryDTO dto);

    AppointmentVO getByID(Long id);
}
