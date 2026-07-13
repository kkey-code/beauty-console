package com.wkr.storeserver.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.entity.Appointment;
import com.wkr.storepojo.entity.AppointmentItem;
import com.wkr.storepojo.entity.InventoryStockLog;
import com.wkr.storepojo.entity.PaymentRecord;
import com.wkr.storepojo.entity.ServiceOrder;
import com.wkr.storepojo.entity.ServiceOrderItem;
import com.wkr.storepojo.entity.ServiceProjectInventory;
import com.wkr.storepojo.entity.SysUser;
import com.wkr.storeserver.mapper.AppointmentItemMapper;
import com.wkr.storeserver.mapper.AppointmentMapper;
import com.wkr.storeserver.mapper.InventoryStockLogMapper;
import com.wkr.storeserver.mapper.PaymentRecordMapper;
import com.wkr.storeserver.mapper.ServiceOrderItemMapper;
import com.wkr.storeserver.mapper.ServiceOrderMapper;
import com.wkr.storeserver.mapper.ServiceProjectInventoryMapper;
import com.wkr.storeserver.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

@Service
public class DeletionGuardService {

    private final AppointmentMapper appointmentMapper;
    private final AppointmentItemMapper appointmentItemMapper;
    private final ServiceOrderMapper serviceOrderMapper;
    private final ServiceOrderItemMapper serviceOrderItemMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final InventoryStockLogMapper inventoryStockLogMapper;
    private final ServiceProjectInventoryMapper serviceProjectInventoryMapper;
    private final SysUserMapper sysUserMapper;

    public DeletionGuardService(
            AppointmentMapper appointmentMapper,
            AppointmentItemMapper appointmentItemMapper,
            ServiceOrderMapper serviceOrderMapper,
            ServiceOrderItemMapper serviceOrderItemMapper,
            PaymentRecordMapper paymentRecordMapper,
            InventoryStockLogMapper inventoryStockLogMapper,
            ServiceProjectInventoryMapper serviceProjectInventoryMapper,
            SysUserMapper sysUserMapper) {
        this.appointmentMapper = appointmentMapper;
        this.appointmentItemMapper = appointmentItemMapper;
        this.serviceOrderMapper = serviceOrderMapper;
        this.serviceOrderItemMapper = serviceOrderItemMapper;
        this.paymentRecordMapper = paymentRecordMapper;
        this.inventoryStockLogMapper = inventoryStockLogMapper;
        this.serviceProjectInventoryMapper = serviceProjectInventoryMapper;
        this.sysUserMapper = sysUserMapper;
    }

    public void assertCustomerCanDelete(Long customerId) {
        if (countAppointmentsByCustomer(customerId) > 0 || countOrdersByCustomer(customerId) > 0) {
            throw new BusinessException("客户已有预约或订单，不能删除，可改为停用或保留历史档案");
        }
    }

    public void assertStaffCanDelete(Long staffId) {
        if (countUsersByStaff(staffId) > 0
                || countAppointmentsByStaff(staffId) > 0
                || countAppointmentItemsByStaff(staffId) > 0
                || countOrderItemsByStaff(staffId) > 0) {
            throw new BusinessException("员工已有账号、预约或订单记录，不能删除，可改为停用");
        }
    }

    public void assertServiceProjectCanDelete(Long serviceProjectId) {
        if (countAppointmentItemsByProject(serviceProjectId) > 0
                || countOrderItemsByProject(serviceProjectId) > 0
                || countProjectInventoriesByProject(serviceProjectId) > 0) {
            throw new BusinessException("服务项目已有预约、订单或耗材配置，不能删除，可改为下架");
        }
    }

    public void assertInventorySkuCanDelete(Long inventoryId) {
        if (countStockLogsByInventory(inventoryId) > 0 || countProjectInventoriesByInventory(inventoryId) > 0) {
            throw new BusinessException("库存耗材已有流水或项目配置，不能删除，可改为停用");
        }
    }

    public void assertAppointmentCanDelete(Long appointmentId) {
        if (countOrdersByAppointment(appointmentId) > 0) {
            throw new BusinessException("预约已生成订单，不能删除");
        }
    }

    public void assertServiceOrderCanDelete(Long orderId) {
        if (countPaymentRecordsByOrder(orderId) > 0 || countStockLogsByOrder(orderId) > 0) {
            throw new BusinessException("订单已有收款或库存流水，不能删除，可取消订单保留历史记录");
        }
    }

    private Long countAppointmentsByCustomer(Long customerId) {
        return appointmentMapper.selectCount(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getCustomerId, customerId));
    }

    private Long countOrdersByCustomer(Long customerId) {
        return serviceOrderMapper.selectCount(new LambdaQueryWrapper<ServiceOrder>()
                .eq(ServiceOrder::getCustomerId, customerId));
    }

    private Long countUsersByStaff(Long staffId) {
        return sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStaffId, staffId));
    }

    private Long countAppointmentsByStaff(Long staffId) {
        return appointmentMapper.selectCount(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getStaffId, staffId));
    }

    private Long countAppointmentItemsByStaff(Long staffId) {
        return appointmentItemMapper.selectCount(new LambdaQueryWrapper<AppointmentItem>()
                .eq(AppointmentItem::getStaffId, staffId));
    }

    private Long countOrderItemsByStaff(Long staffId) {
        return serviceOrderItemMapper.selectCount(new LambdaQueryWrapper<ServiceOrderItem>()
                .eq(ServiceOrderItem::getStaffId, staffId));
    }

    private Long countAppointmentItemsByProject(Long serviceProjectId) {
        return appointmentItemMapper.selectCount(new LambdaQueryWrapper<AppointmentItem>()
                .eq(AppointmentItem::getServiceProjectId, serviceProjectId));
    }

    private Long countOrderItemsByProject(Long serviceProjectId) {
        return serviceOrderItemMapper.selectCount(new LambdaQueryWrapper<ServiceOrderItem>()
                .eq(ServiceOrderItem::getServiceProjectId, serviceProjectId));
    }

    private Long countProjectInventoriesByProject(Long serviceProjectId) {
        return serviceProjectInventoryMapper.selectCount(new LambdaQueryWrapper<ServiceProjectInventory>()
                .eq(ServiceProjectInventory::getServiceProjectId, serviceProjectId));
    }

    private Long countStockLogsByInventory(Long inventoryId) {
        return inventoryStockLogMapper.selectCount(new LambdaQueryWrapper<InventoryStockLog>()
                .eq(InventoryStockLog::getInventoryId, inventoryId));
    }

    private Long countProjectInventoriesByInventory(Long inventoryId) {
        return serviceProjectInventoryMapper.selectCount(new LambdaQueryWrapper<ServiceProjectInventory>()
                .eq(ServiceProjectInventory::getInventoryId, inventoryId));
    }

    private Long countOrdersByAppointment(Long appointmentId) {
        return serviceOrderMapper.selectCount(new LambdaQueryWrapper<ServiceOrder>()
                .eq(ServiceOrder::getAppointmentId, appointmentId));
    }

    private Long countPaymentRecordsByOrder(Long orderId) {
        return paymentRecordMapper.selectCount(new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getOrderId, orderId));
    }

    private Long countStockLogsByOrder(Long orderId) {
        return inventoryStockLogMapper.selectCount(new LambdaQueryWrapper<InventoryStockLog>()
                .eq(InventoryStockLog::getRelatedOrderId, orderId));
    }
}
