package com.wkr.storeserver.handler;

import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storecommon.exception.SystemException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request =
            new MockHttpServletRequest("POST", "/admin/test");

    @Test
    void businessExceptionReturnsConflict() {
        ResponseEntity<Result<?>> response = handler.handleBusinessException(
                new BusinessException("当前状态不允许该操作"), request);

        assertError(response, HttpStatus.CONFLICT, "当前状态不允许该操作");
    }

    @Test
    void systemExceptionReturnsInternalServerError() {
        ResponseEntity<Result<?>> response = handler.handleSystemException(
                new SystemException("数据库更新失败"), request);

        assertError(response, HttpStatus.INTERNAL_SERVER_ERROR, "数据库更新失败");
    }

    @Test
    void unexpectedExceptionDoesNotExposeImplementationDetails() {
        ResponseEntity<Result<?>> response = handler.handleException(
                new IllegalStateException("secret implementation detail"), request);

        assertError(response, HttpStatus.INTERNAL_SERVER_ERROR, "系统错误");
    }

    private void assertError(
            ResponseEntity<Result<?>> response,
            HttpStatus expectedStatus,
            String expectedMessage) {
        assertEquals(expectedStatus, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedStatus.value(), response.getBody().getCode());
        assertEquals(expectedMessage, response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }
}
