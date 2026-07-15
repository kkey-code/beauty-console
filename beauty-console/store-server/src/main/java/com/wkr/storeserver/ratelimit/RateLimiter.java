package com.wkr.storeserver.ratelimit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 声明接口的分布式令牌桶限流规则。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {

    /** 业务限流键；留空时使用类名和方法名。 */
    String key() default "";

    /** 时间窗口内允许的最大请求数。 */
    long maxRequests();

    /** 时间窗口大小。 */
    long window();

    /** 时间窗口单位。 */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /** 是否按当前登录用户拆分令牌桶。 */
    boolean perUser() default true;

    /** 超过阈值时返回的提示。 */
    String message() default "请求太频繁，请稍后再试";
}
