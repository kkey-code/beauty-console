package com.wkr.storeserver.ratelimit;

import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

/**
 * 将 {@link RateLimiter} 转换为所有应用实例共享的 Redis 令牌桶。
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimiterAspect {

    private static final String KEY_PREFIX = "rate_limiter:";

    private final RedissonClient redissonClient;

    public RateLimiterAspect(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Around("@annotation(rateLimiter)")
    public Object limit(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {
        validate(rateLimiter);

        Duration interval = Duration.of(rateLimiter.window(), rateLimiter.timeUnit().toChronoUnit());
        Duration keepAlive = interval.multipliedBy(10);
        RRateLimiter limiter = redissonClient.getRateLimiter(buildLimiterKey(joinPoint, rateLimiter));

        limiter.trySetRate(RateType.OVERALL, rateLimiter.maxRequests(), interval, keepAlive);
        if (!limiter.tryAcquire()) {
            throw new RateLimitExceededException(rateLimiter.message());
        }

        return joinPoint.proceed();
    }

    private String buildLimiterKey(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) {
        String businessKey = rateLimiter.key();
        if (!StringUtils.hasText(businessKey)) {
            businessKey = joinPoint.getSignature().getDeclaringTypeName()
                    + ":" + joinPoint.getSignature().getName();
        }

        if (!rateLimiter.perUser()) {
            return KEY_PREFIX + businessKey + ":global";
        }

        Long userId = BaseContext.getCurrentUserId();
        if (userId != null) {
            return KEY_PREFIX + businessKey + ":user:" + userId;
        }

        return KEY_PREFIX + businessKey + ":ip:" + currentClientIp();
    }

    private String currentClientIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (StringUtils.hasText(forwardedFor)) {
                return forwardedFor.split(",", 2)[0].trim();
            }
            return request.getRemoteAddr();
        }
        return "unknown";
    }

    private void validate(RateLimiter rateLimiter) {
        if (rateLimiter.maxRequests() <= 0 || rateLimiter.window() <= 0) {
            throw new IllegalArgumentException("限流次数和时间窗口必须大于 0");
        }
    }
}
