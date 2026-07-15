package com.wkr.storeserver.idempotency;

import java.util.function.Function;

/**
 * 订单幂等服务：签发一次性令牌，并保证相同令牌只执行一次下单逻辑。
 */
public interface OrderIdempotencyService {

    String IDEMPOTENCY_HEADER = "Idempotency-Key";

    /**
     * 为当前登录用户签发一个短期有效的下单令牌。
     */
    String issueToken();

    /**
     * 使用令牌执行下单。重复请求在成功后返回第一次创建的订单 ID。
     */
    Long execute(String token, Function<String, Long> orderAction);
}
