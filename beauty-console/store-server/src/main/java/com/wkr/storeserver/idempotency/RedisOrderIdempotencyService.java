package com.wkr.storeserver.idempotency;

import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.exception.DuplicateSubmissionException;
import com.wkr.storecommon.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * 使用 Redis 状态机实现订单幂等。
 *
 * <p>状态流转：READY -> PROCESSING -> SUCCESS:orderId。状态切换通过 Lua 脚本原子完成，
 * 避免“先判断存在、再删除”产生的并发窗口。</p>
 */
@Service
@Slf4j
public class RedisOrderIdempotencyService implements OrderIdempotencyService {

    private static final String KEY_PREFIX = "idempotency:service-order:user:";
    private static final String READY = "READY";
    private static final String PROCESSING = "PROCESSING";
    private static final String SUCCESS_PREFIX = "SUCCESS:";
    private static final String CLAIMED = "CLAIMED";
    private static final String MISSING = "MISSING";

    private static final Duration READY_TTL = Duration.ofMinutes(10);
    private static final Duration PROCESSING_TTL = Duration.ofMinutes(5);
    private static final Duration SUCCESS_TTL = Duration.ofHours(24);

    private static final DefaultRedisScript<String> CLAIM_SCRIPT = new DefaultRedisScript<>("""
            local value = redis.call('GET', KEYS[1])
            if not value then
                return 'MISSING'
            end
            if value == 'READY' then
                redis.call('SET', KEYS[1], 'PROCESSING', 'EX', ARGV[1])
                return 'CLAIMED'
            end
            return value
            """, String.class);

    private static final DefaultRedisScript<Long> COMPLETE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[1]) == 'PROCESSING' then
                redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[2])
                return 1
            end
            return 0
            """, Long.class);

    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[1]) == 'PROCESSING' then
                redis.call('SET', KEYS[1], 'READY', 'EX', ARGV[1])
                return 1
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public RedisOrderIdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String issueToken() {
        Long userId = currentUserId();
        for (int attempt = 0; attempt < 3; attempt++) {
            String token = UUID.randomUUID().toString();
            Boolean created = redisTemplate.opsForValue()
                    .setIfAbsent(buildKey(userId, token), READY, READY_TTL);
            if (Boolean.TRUE.equals(created)) {
                return token;
            }
        }
        throw new SystemException("生成下单幂等令牌失败，请稍后重试");
    }

    @Override
    public Long execute(String token, Function<String, Long> orderAction) {
        String normalizedToken = normalizeToken(token);
        String key = buildKey(currentUserId(), normalizedToken);
        String state = redisTemplate.execute(
                CLAIM_SCRIPT,
                List.of(key),
                String.valueOf(PROCESSING_TTL.toSeconds()));

        if (state != null && state.startsWith(SUCCESS_PREFIX)) {
            return completedOrderId(state);
        }
        if (PROCESSING.equals(state)) {
            throw new DuplicateSubmissionException("订单正在处理中，请勿重复提交");
        }
        if (!CLAIMED.equals(state)) {
            throw new DuplicateSubmissionException("幂等令牌无效或已过期，请重新获取");
        }

        Long orderId;
        try {
            orderId = orderAction.apply(normalizedToken);
        } catch (RuntimeException exception) {
            release(key);
            throw exception;
        }

        if (orderId == null) {
            release(key);
            throw new SystemException("订单创建结果异常");
        }

        Long completed = redisTemplate.execute(
                COMPLETE_SCRIPT,
                List.of(key),
                SUCCESS_PREFIX + orderId,
                String.valueOf(SUCCESS_TTL.toSeconds()));
        if (!Long.valueOf(1L).equals(completed)) {
            throw new SystemException("订单已创建，但幂等状态保存失败");
        }
        return orderId;
    }

    private void release(String key) {
        try {
            redisTemplate.execute(
                    RELEASE_SCRIPT,
                    List.of(key),
                    String.valueOf(READY_TTL.toSeconds()));
        } catch (RuntimeException exception) {
            log.warn("释放下单幂等令牌失败: key={}", key, exception);
        }
    }

    private Long completedOrderId(String state) {
        try {
            return Long.valueOf(state.substring(SUCCESS_PREFIX.length()));
        } catch (NumberFormatException exception) {
            throw new SystemException("下单幂等状态异常");
        }
    }

    private String normalizeToken(String token) {
        if (token == null || token.isBlank()) {
            throw new DuplicateSubmissionException("缺少下单幂等令牌");
        }
        String normalized = token.trim().toLowerCase();
        try {
            if (!UUID.fromString(normalized).toString().equals(normalized)) {
                throw new IllegalArgumentException("UUID 格式不规范");
            }
            return normalized;
        } catch (IllegalArgumentException exception) {
            throw new DuplicateSubmissionException("下单幂等令牌格式错误");
        }
    }

    private Long currentUserId() {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            throw new SystemException("无法识别当前登录用户");
        }
        return userId;
    }

    private String buildKey(Long userId, String token) {
        return KEY_PREFIX + userId + ":token:" + token;
    }
}
