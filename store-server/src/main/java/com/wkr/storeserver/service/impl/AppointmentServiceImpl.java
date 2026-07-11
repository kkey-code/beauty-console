package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.AppointmentDTO;
import com.wkr.storepojo.dto.AppointmentItemDTO;
import com.wkr.storepojo.dto.AppointmentPageQueryDTO;
import com.wkr.storepojo.entity.Appointment;
import com.wkr.storepojo.entity.AppointmentItem;
import com.wkr.storepojo.entity.CustomerProfile;
import com.wkr.storepojo.entity.StaffMember;
import com.wkr.storepojo.enums.AppointmentEventEnum;
import com.wkr.storepojo.enums.AppointmentStatusEnum;
import com.wkr.storepojo.vo.AppointmentItemVO;
import com.wkr.storepojo.vo.AppointmentVO;
import com.wkr.storeserver.mapper.AppointmentMapper;
import com.wkr.storeserver.mapper.CustomerProfileMapper;
import com.wkr.storeserver.service.AppointmentItemService;
import com.wkr.storeserver.service.AppointmentService;
import com.wkr.storeserver.service.DeletionGuardService;
import com.wkr.storeserver.service.StaffMemberService;
import com.wkr.storeserver.support.BusinessNoGenerator;
import com.wkr.storeserver.transition.AppointmentStatusTransition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
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
    @Autowired
    private DeletionGuardService deletionGuardService;

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

    @Override
    @Transactional
    public boolean createAppointment(AppointmentDTO dto) {
        Appointment appointment = new Appointment();
        BeanUtils.copyProperties(dto, appointment);
        fillAddDefaults(appointment);

        boolean saved = save(appointment);
        if (!saved) {
            throw new BusinessException("新增预约失败");
        }

        if (dto.getItems() != null) {
            for (AppointmentItemDTO item : dto.getItems()) {
                AppointmentItem appointmentItem = new AppointmentItem();
                BeanUtils.copyProperties(item, appointmentItem);
                appointmentItem.setAppointmentId(appointment.getId());
                appointmentItem.setCreateTime(LocalDateTime.now());
                appointmentItemService.save(appointmentItem);
            }
        }
        return true;
    }

    @Override
    public boolean updateAppointment(Long id, AppointmentDTO dto) {
        getExistingAppointment(id);
        Appointment appointment = new Appointment();
        BeanUtils.copyProperties(dto, appointment);
        appointment.setId(id);
        appointment.setUpdateTime(LocalDateTime.now());
        return updateById(appointment);
    }

    @Override
    @Transactional
    public boolean confirm(Long id) {
        return applyEvent(id, AppointmentEventEnum.CONFIRM);
    }

    @Override
    @Transactional
    public boolean complete(Long id) {
        return applyEvent(id, AppointmentEventEnum.COMPLETE);
    }

    @Override
    @Transactional
    public boolean cancel(Long id) {
        return applyEvent(id, AppointmentEventEnum.CANCEL);
    }

    @Override
    public boolean deleteAppointment(Long id) {
        getExistingAppointment(id);
        deletionGuardService.assertAppointmentCanDelete(id);
        return removeById(id);
    }

    private boolean applyEvent(Long id, AppointmentEventEnum event) {
        Appointment appointment = getExistingAppointmentForUpdate(id);
        AppointmentStatusEnum current = statusOf(appointment.getStatus());
        AppointmentStatusEnum next = AppointmentStatusTransition.next(current, event);

        Appointment update = new Appointment();
        update.setId(id);
        update.setStatus(next.getCode());
        update.setUpdateTime(LocalDateTime.now());
        return updateById(update);
    }

    private Appointment getExistingAppointment(Long id) {
        Appointment appointment = getById(id);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        return appointment;
    }

    private Appointment getExistingAppointmentForUpdate(Long id) {
        Appointment appointment = getOne(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getId, id)
                .last("FOR UPDATE"));
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        return appointment;
    }

    private void fillAddDefaults(Appointment appointment) {
        LocalDateTime now = LocalDateTime.now();
        if (!StringUtils.hasText(appointment.getAppointmentNo())) {
            appointment.setAppointmentNo(BusinessNoGenerator.next("APT"));
        }
        if (appointment.getStaffId() == null) {
            appointment.setStaffId(BaseContext.getCurrentId());
        }
        if (appointment.getStaffId() == null) {
            throw new BusinessException("主服务员工不能为空");
        }
        appointment.setCreateTime(now);
        appointment.setUpdateTime(now);
    }

    private AppointmentStatusEnum statusOf(Integer status) {
        for (AppointmentStatusEnum item : AppointmentStatusEnum.values()) {
            if (item.matches(status)) {
                return item;
            }
        }
        throw new BusinessException("预约状态错误");
    }
}
