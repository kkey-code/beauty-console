package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.AppointmentPageQueryDTO;
import com.wkr.storepojo.entity.Appointment;
import com.wkr.storepojo.entity.AppointmentItem;
import com.wkr.storepojo.entity.CustomerProfile;
import com.wkr.storepojo.entity.StaffMember;
import com.wkr.storepojo.enums.AppointmentStatusEnum;
import com.wkr.storepojo.vo.AppointmentItemVO;
import com.wkr.storepojo.vo.AppointmentVO;
import com.wkr.storeserver.mapper.AppointmentMapper;
import com.wkr.storeserver.mapper.CustomerProfileMapper;
import com.wkr.storeserver.service.AppointmentItemService;
import com.wkr.storeserver.service.AppointmentService;
import com.wkr.storeserver.service.StaffMemberService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 预约服务实现，封装该业务模块的查询、校验、状态更新和持久化流程。
 */
@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;
    @Autowired
    private CustomerProfileMapper customerProfileMapper;
    @Autowired
    private StaffMemberService staffMemberService;
    @Autowired
    private AppointmentItemService appointmentItemService;

    @Override
    public Result<PageResult<AppointmentVO>> List(AppointmentPageQueryDTO dto) {
        Page<AppointmentVO> page = new Page<>(dto.getPage(), dto.getPageSize());
        IPage<AppointmentVO> records = appointmentMapper.list(page, dto);

        List<AppointmentVO> appointmentVOS = new ArrayList<>();
        for (AppointmentVO appointmentVO : records.getRecords()) {
            appointmentVO.setStatusName(AppointmentStatusEnum.labelOf(appointmentVO.getStatus()));
            appointmentVOS.add(appointmentVO);
        }
        return Result.success(PageResult.<AppointmentVO>builder()
                .total(records.getTotal())
                .records(appointmentVOS)
                .build());
    }

    @Override
    public AppointmentVO getByID(Long id) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }

        CustomerProfile customerProfile = customerProfileMapper.selectById(appointment.getCustomerId());
        StaffMember staffMember = staffMemberService.getById(appointment.getStaffId());

        AppointmentVO appointmentVO = new AppointmentVO();
        BeanUtils.copyProperties(appointment, appointmentVO);
        if (customerProfile != null) {
            appointmentVO.setCustomerName(customerProfile.getName());
            appointmentVO.setCustomerPhone(customerProfile.getPhone());
        }
        if (staffMember != null) {
            appointmentVO.setStaffName(staffMember.getName());
        }
        appointmentVO.setStatusName(AppointmentStatusEnum.labelOf(appointmentVO.getStatus()));
        appointmentVO.setItems(getAppointmentItems(id));
        return appointmentVO;
    }

    private List<AppointmentItemVO> getAppointmentItems(Long appointmentId) {
        List<AppointmentItem> items = appointmentItemService.list(new LambdaQueryWrapper<AppointmentItem>()
                .eq(AppointmentItem::getAppointmentId, appointmentId));

        List<AppointmentItemVO> itemVOS = new ArrayList<>();
        for (AppointmentItem item : items) {
            AppointmentItemVO itemVO = new AppointmentItemVO();
            BeanUtils.copyProperties(item, itemVO);
            itemVOS.add(itemVO);
        }
        return itemVOS;
    }
}
