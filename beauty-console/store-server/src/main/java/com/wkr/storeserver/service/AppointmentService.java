package com.wkr.storeserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storepojo.dto.AppointmentDTO;
import com.wkr.storepojo.dto.AppointmentPageQueryDTO;
import com.wkr.storepojo.entity.Appointment;
import com.wkr.storepojo.vo.AppointmentVO;

import java.util.List;

/**
* @author kkey
* @description 针对表【appointment(预约主表)】的数据库操作Service
* @createDate 2026-07-03 21:10:41
*/
public interface AppointmentService extends IService<Appointment> {

    Result<PageResult<AppointmentVO>> List(AppointmentPageQueryDTO dto);

    List<AppointmentVO> listRecent(int limit);

    AppointmentVO getByID(Long id);

    boolean createAppointment(AppointmentDTO dto);

    boolean updateAppointment(Long id, AppointmentDTO dto);

    boolean confirm(Long id);

    boolean complete(Long id);

    boolean cancel(Long id);

    boolean deleteAppointment(Long id);
}
