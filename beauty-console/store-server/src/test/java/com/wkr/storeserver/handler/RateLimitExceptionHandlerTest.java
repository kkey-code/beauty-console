package com.wkr.storeserver.handler;

import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RateLimitExceptionHandlerTest {

    @Test
    void returnsHttp429AndUnifiedErrorBody() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin/service-orders");

        ResponseEntity<Result<?>> response = handler.handleRateLimitExceededException(
                new RateLimitExceededException("请求太频繁，请稍后再试"),
                request);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getCode());
        assertEquals("请求太频繁，请稍后再试", response.getBody().getMsg());
    }
}
