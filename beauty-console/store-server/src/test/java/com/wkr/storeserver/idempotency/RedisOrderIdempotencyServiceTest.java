package com.wkr.storeserver.idempotency;

import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storecommon.exception.DuplicateSubmissionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisOrderIdempotencyServiceTest {

    private static final String TOKEN = "00000000-0000-0000-0000-000000000042";

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisOrderIdempotencyService service;

    @BeforeEach
    void setUp() {
        BaseContext.setCurrentUser(42L, 7L, 1, "SUPER_ADMIN");
        service = new RedisOrderIdempotencyService(redisTemplate);
    }

    @AfterEach
    void tearDown() {
        BaseContext.remove();
    }

    @Test
    void issueTokenStoresReadyStateForCurrentUser() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("READY"), eq(Duration.ofMinutes(10))))
                .thenReturn(true);

        String token = service.issueToken();

        assertEquals(token, UUID.fromString(token).toString());
        verify(valueOperations).setIfAbsent(
                eq("idempotency:service-order:user:42:token:" + token),
                eq("READY"),
                eq(Duration.ofMinutes(10)));
    }

    @Test
    void concurrentRequestsExecuteBusinessOnlyOnceAndReplayOrderId() throws Exception {
        AtomicReference<String> state = new AtomicReference<>("READY");
        simulateAtomicRedis(state);
        AtomicInteger executions = new AtomicInteger();
        CountDownLatch actionStarted = new CountDownLatch(1);
        CountDownLatch allowActionToFinish = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            Future<Long> first = executor.submit(() -> {
                BaseContext.setCurrentUser(42L, 7L, 1, "SUPER_ADMIN");
                try {
                    return service.execute(TOKEN, requestId -> {
                        executions.incrementAndGet();
                        actionStarted.countDown();
                        await(allowActionToFinish);
                        return 88L;
                    });
                } finally {
                    BaseContext.remove();
                }
            });

            assertTrue(actionStarted.await(3, TimeUnit.SECONDS));
            assertThrows(DuplicateSubmissionException.class,
                    () -> service.execute(TOKEN, requestId -> 99L));

            allowActionToFinish.countDown();
            assertEquals(88L, first.get(3, TimeUnit.SECONDS));
            assertEquals(88L, service.execute(TOKEN, requestId -> 99L));
            assertEquals(1, executions.get());
            assertEquals("SUCCESS:88", state.get());
        } finally {
            allowActionToFinish.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void failedBusinessReleasesTokenForRetry() {
        AtomicReference<String> state = new AtomicReference<>("READY");
        simulateAtomicRedis(state);

        assertThrows(BusinessException.class,
                () -> service.execute(TOKEN, requestId -> {
                    throw new BusinessException("订单参数错误");
                }));

        assertEquals("READY", state.get());
        assertEquals(66L, service.execute(TOKEN, requestId -> 66L));
    }

    @Test
    void missingOrMalformedTokenIsRejected() {
        AtomicReference<String> missing = new AtomicReference<>();
        simulateAtomicRedis(missing);

        assertThrows(DuplicateSubmissionException.class,
                () -> service.execute(TOKEN, requestId -> 1L));
        assertThrows(DuplicateSubmissionException.class,
                () -> service.execute("not-a-uuid", requestId -> 1L));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void simulateAtomicRedis(AtomicReference<String> state) {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenAnswer(invocation -> {
                    RedisScript<?> script = invocation.getArgument(0);
                    Object[] arguments = Arrays.copyOfRange(
                            invocation.getArguments(),
                            2,
                            invocation.getArguments().length);
                    synchronized (state) {
                        if (String.class.equals(script.getResultType())) {
                            String current = state.get();
                            if (current == null) {
                                return "MISSING";
                            }
                            if ("READY".equals(current)) {
                                state.set("PROCESSING");
                                return "CLAIMED";
                            }
                            return current;
                        }

                        if (arguments.length == 2 && "PROCESSING".equals(state.get())) {
                            state.set(String.valueOf(arguments[0]));
                            return 1L;
                        }
                        if (arguments.length == 1 && "PROCESSING".equals(state.get())) {
                            state.set("READY");
                            return 1L;
                        }
                        return 0L;
                    }
                });
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(3, TimeUnit.SECONDS)) {
                throw new IllegalStateException("等待并发测试信号超时");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("并发测试被中断", exception);
        }
    }
}
