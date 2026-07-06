package com.wkr.storeserver.interceptor;

import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.properties.JwtProperties;
import com.wkr.storecommon.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;

    public JwtTokenInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {

        // 1. 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2. 从配置的请求头名称获取 token
        String tokenHeader = request.getHeader(jwtProperties.getAdminTokenName());

        // 3. 没有 token 头 → 返回 401
        if (tokenHeader == null || tokenHeader.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 4. 去掉 "Bearer " 前缀
        String token;
        if (tokenHeader.startsWith("Bearer ")) {
            token = tokenHeader.substring(7);
        } else {
            token = tokenHeader;
        }

        // 5. token 为空 → 返回 401
        if (token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 6. 解析 token
        Claims claims;
        try {
            claims = JwtUtils.parseJWT(jwtProperties.getAdminSecretKey(), token);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 7. 从 claims 取 staffId
        Object staffIdObj = claims.get("staffId");
        if (staffIdObj == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 8. 转为 Long 并存入 ThreadLocal
        Long staffId = Long.valueOf(staffIdObj.toString());
        BaseContext.setCurrentId(staffId);

        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex)  {
        BaseContext.remove();
    }
}