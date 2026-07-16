package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.entity.CustomerProfile;
import com.wkr.storeserver.service.CustomerProfileService;
import com.wkr.storeserver.mapper.CustomerProfileMapper;
import com.wkr.storeserver.support.DataScopeSupport;
import org.springframework.stereotype.Service;

/**
* @author kkey
* @description 针对表【customer_profile(客户档案表)】的数据库操作Service实现
* @createDate 2026-07-03 21:29:31
*/
@Service
public class CustomerProfileServiceImpl extends ServiceImpl<CustomerProfileMapper, CustomerProfile>
    implements CustomerProfileService{

    @Override
    public IPage<CustomerProfile> pageVisible(
            Page<CustomerProfile> page,
            LambdaQueryWrapper<CustomerProfile> wrapper) {
        return page(page, applyDataScope(wrapper));
    }

    @Override
    public CustomerProfile getVisibleById(Long id) {
        CustomerProfile customer = getById(id);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        assertCanAccess(id);
        return customer;
    }

    @Override
    public long countVisibleCustomers() {
        return count(applyDataScope(new LambdaQueryWrapper<>()));
    }

    @Override
    public void assertCanAccess(Long id) {
        if (!DataScopeSupport.isStaffSelfScope()) {
            return;
        }
        long visible = count(applyDataScope(new LambdaQueryWrapper<CustomerProfile>()
                .eq(CustomerProfile::getId, id)));
        if (visible == 0) {
            throw new BusinessException("无权访问其他员工的客户档案");
        }
    }

    private LambdaQueryWrapper<CustomerProfile> applyDataScope(LambdaQueryWrapper<CustomerProfile> wrapper) {
        Long staffId = DataScopeSupport.currentScopedStaffId();
        if (staffId == null) {
            return wrapper;
        }
        return wrapper.and(scope -> scope
                .eq(CustomerProfile::getOwnerStaffId, staffId)
                .or()
                .exists("SELECT 1 FROM appointment a_scope "
                        + "WHERE a_scope.customer_id = customer_profile.id "
                        + "AND a_scope.deleted = 0 "
                        + "AND (a_scope.staff_id = {0} OR EXISTS ("
                        + "SELECT 1 FROM appointment_item ai_scope "
                        + "WHERE ai_scope.appointment_id = a_scope.id AND ai_scope.staff_id = {0}))", staffId)
                .or()
                .exists("SELECT 1 FROM service_order so_scope "
                        + "JOIN service_order_item soi_scope ON soi_scope.order_id = so_scope.id "
                        + "WHERE so_scope.customer_id = customer_profile.id "
                        + "AND so_scope.deleted = 0 AND soi_scope.staff_id = {0}", staffId));
    }
}




