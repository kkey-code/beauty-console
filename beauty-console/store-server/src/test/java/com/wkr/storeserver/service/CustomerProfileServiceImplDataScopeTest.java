package com.wkr.storeserver.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wkr.storecommon.common.BaseContext;
import com.wkr.storepojo.entity.CustomerProfile;
import com.wkr.storeserver.mapper.CustomerProfileMapper;
import com.wkr.storeserver.service.impl.CustomerProfileServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerProfileServiceImplDataScopeTest {

    @Mock
    private CustomerProfileMapper customerProfileMapper;

    private CustomerProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), "customer-profile-scope-test"),
                CustomerProfile.class);
        service = new CustomerProfileServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", customerProfileMapper);
    }

    @AfterEach
    void clearContext() {
        BaseContext.remove();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void staffCountIncludesOwnedAppointmentAndOrderCustomers() {
        BaseContext.setCurrentUser(9L, 1003L, 3, "STAFF");
        when(customerProfileMapper.selectCount(any(Wrapper.class))).thenReturn(3L);

        service.countVisibleCustomers();

        ArgumentCaptor<Wrapper<CustomerProfile>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(customerProfileMapper).selectCount(captor.capture());
        String sql = captor.getValue().getSqlSegment();
        assertTrue(sql.contains("owner_staff_id"));
        assertTrue(sql.contains("appointment_item"));
        assertTrue(sql.contains("service_order_item"));
    }
}
