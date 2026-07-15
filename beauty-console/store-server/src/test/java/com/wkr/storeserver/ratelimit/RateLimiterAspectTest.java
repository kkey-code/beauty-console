package com.wkr.storeserver.ratelimit;

import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.exception.RateLimitExceededException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimiterAspectTest {

    private final RedissonClient redissonClient = mock(RedissonClient.class);
    private final RRateLimiter redisRateLimiter = mock(RRateLimiter.class);
    private final ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    private final RateLimiterAspect aspect = new RateLimiterAspect(redissonClient);

    @AfterEach
    void clearContext() {
        BaseContext.remove();
    }

    @Test
    void allowsRequestWhenTokenIsAvailable() throws Throwable {
        BaseContext.setCurrentUser(42L, 7L, 1, "SUPER_ADMIN");
        RateLimiter rule = rule();
        when(redissonClient.getRateLimiter("rate_limiter:service-order:create:user:42"))
                .thenReturn(redisRateLimiter);
        when(redisRateLimiter.tryAcquire()).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("created");

        Object result = aspect.limit(joinPoint, rule);

        assertEquals("created", result);
        verify(redisRateLimiter).trySetRate(
                RateType.OVERALL,
                10,
                Duration.ofMinutes(1),
                Duration.ofMinutes(10));
        verify(joinPoint).proceed();
    }

    @Test
    void rejectsRequestWithoutCallingBusinessMethodWhenBucketIsEmpty() throws Throwable {
        BaseContext.setCurrentUser(42L, 7L, 1, "SUPER_ADMIN");
        when(redissonClient.getRateLimiter("rate_limiter:service-order:create:user:42"))
                .thenReturn(redisRateLimiter);
        when(redisRateLimiter.tryAcquire()).thenReturn(false);

        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                () -> aspect.limit(joinPoint, rule()));

        assertEquals("请求太频繁，请稍后再试", exception.getMessage());
        verify(joinPoint, never()).proceed();
    }

    private RateLimiter rule() throws NoSuchMethodException {
        Method method = LimitedService.class.getDeclaredMethod("create");
        return method.getAnnotation(RateLimiter.class);
    }

    private static class LimitedService {

        @RateLimiter(
                key = "service-order:create",
                maxRequests = 10,
                window = 1,
                timeUnit = TimeUnit.MINUTES)
        void create() {
        }
    }
}
