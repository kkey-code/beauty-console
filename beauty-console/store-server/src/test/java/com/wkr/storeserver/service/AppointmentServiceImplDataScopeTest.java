package com.wkr.storeserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wkr.storecommon.common.BaseContext;
import com.wkr.storepojo.dto.AppointmentPageQueryDTO;
import com.wkr.storepojo.vo.AppointmentVO;
import com.wkr.storeserver.mapper.AppointmentMapper;
import com.wkr.storeserver.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplDataScopeTest {

    @Mock
    private AppointmentMapper appointmentMapper;

    @AfterEach
    void clearContext() {
        BaseContext.remove();
    }

    @Test
    void staffAppointmentPagePassesBoundEmployeeScopeToMapper() {
        AppointmentServiceImpl service = new AppointmentServiceImpl();
        ReflectionTestUtils.setField(service, "appointmentMapper", appointmentMapper);
        ReflectionTestUtils.setField(service, "baseMapper", appointmentMapper);
        when(appointmentMapper.list(any(Page.class), any(AppointmentPageQueryDTO.class), eq(1003L)))
                .thenAnswer(invocation -> {
                    IPage<AppointmentVO> page = invocation.getArgument(0);
                    page.setTotal(0);
                    return page;
                });
        BaseContext.setCurrentUser(9L, 1003L, 3, "STAFF");

        service.List(new AppointmentPageQueryDTO());

        verify(appointmentMapper).list(any(Page.class), any(AppointmentPageQueryDTO.class), eq(1003L));
    }
}
