package com.wkr.storeserver.support;

import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.enums.RoleEnum;

/**
 * 当前登录人的数据范围。普通员工只能访问与其员工档案关联的业务数据。
 */
public final class DataScopeSupport {

    private DataScopeSupport() {
    }

    public static boolean isStaffSelfScope() {
        return RoleEnum.STAFF.matches(BaseContext.getCurrentRole());
    }

    public static Long currentScopedStaffId() {
        if (!isStaffSelfScope()) {
            return null;
        }
        Long staffId = BaseContext.getCurrentId();
        if (staffId == null) {
            throw new BusinessException("普通员工账号未关联员工，无法访问业务数据");
        }
        return staffId;
    }
}
