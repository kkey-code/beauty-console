package com.wkr.storeserver.support;

import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataScopeSupportTest {

    @AfterEach
    void clearContext() {
        BaseContext.remove();
    }

    @Test
    void staffUsesBoundEmployeeAsDataScope() {
        BaseContext.setCurrentUser(1L, 1003L, 3, "STAFF");

        assertEquals(1003L, DataScopeSupport.currentScopedStaffId());
    }

    @Test
    void managerIsNotRestrictedToOneEmployee() {
        BaseContext.setCurrentUser(1L, 1002L, 2, "STORE_MANAGER");

        assertNull(DataScopeSupport.currentScopedStaffId());
    }

    @Test
    void unboundStaffAccountIsRejectedInsteadOfSeeingAllData() {
        BaseContext.setCurrentUser(1L, null, 3, "STAFF");

        assertThrows(BusinessException.class, DataScopeSupport::currentScopedStaffId);
    }
}
