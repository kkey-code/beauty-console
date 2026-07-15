package com.wkr.storeserver.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.common.JwtClaimsConstant;
import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.properties.JwtProperties;
import com.wkr.storecommon.util.JwtUtils;
import com.wkr.storepojo.entity.SysUser;
import com.wkr.storepojo.enums.RoleEnum;
import com.wkr.storeserver.mapper.SysUserMapper;
import com.wkr.storeserver.security.RolePermissionChecker;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Objects;

/**
 * 管理端 JWT 拦截器，完成身份校验、当前用户上下文初始化和角色权限检查。
 */
@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;
    private final RolePermissionChecker rolePermissionChecker;
    private final SysUserMapper sysUserMapper;
    private final ObjectMapper objectMapper;

    public JwtTokenInterceptor(
            JwtProperties jwtProperties,
            RolePermissionChecker rolePermissionChecker,
            SysUserMapper sysUserMapper,
            ObjectMapper objectMapper) {
        this.jwtProperties = jwtProperties;
        this.rolePermissionChecker = rolePermissionChecker;
        this.sysUserMapper = sysUserMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        BaseContext.remove();
        String tokenHeader = request.getHeader(jwtProperties.getAdminTokenName());
        if (tokenHeader == null || tokenHeader.isBlank()) {
            return reject(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录或登录已过期");
        }

        String token = tokenHeader.startsWith("Bearer ")
                ? tokenHeader.substring(7).trim()
                : tokenHeader.trim();
        if (token.isEmpty()) {
            return reject(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录或登录已过期");
        }

        try {
            Claims claims = JwtUtils.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long userId = longClaim(claims, JwtClaimsConstant.USER_ID);
            Long staffId = longClaim(claims, JwtClaimsConstant.STAFF_ID);
            Integer roleId = integerClaim(claims, JwtClaimsConstant.ROLE_ID);
            Integer status = integerClaim(claims, JwtClaimsConstant.STATUS);
            String roleCode = stringClaim(claims, JwtClaimsConstant.ROLE_CODE);
            RoleEnum role = RoleEnum.fromRoleCode(roleCode);

            if (userId == null
                    || roleId == null
                    || status == null
                    || status != 1
                    || role == null
                    || !role.matches(roleId)) {
                return reject(response, HttpServletResponse.SC_UNAUTHORIZED, "登录信息无效");
            }

            SysUser currentUser = sysUserMapper.selectById(userId);
            if (currentUser == null || currentUser.getStatus() == null || currentUser.getStatus() != 1) {
                return reject(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录或登录已过期");
            }
            RoleEnum currentRole = RoleEnum.fromCode(currentUser.getRoleId());
            if (currentRole == null
                    || !currentRole.matches(roleId)
                    || !currentRole.matches(roleCode)
                    || !Objects.equals(currentUser.getStaffId(), staffId)) {
                return reject(response, HttpServletResponse.SC_UNAUTHORIZED, "登录信息已变更，请重新登录");
            }

            BaseContext.setCurrentUser(userId, currentUser.getStaffId(), currentRole.getCode(), currentRole.getRoleCode());

            String requestPath = requestPath(request);
            if (!rolePermissionChecker.isAllowed(currentRole, userId, request.getMethod(), requestPath)) {
                BaseContext.remove();
                return reject(response, HttpServletResponse.SC_FORBIDDEN, "无权限访问该接口");
            }
            return true;
        } catch (Exception e) {
            BaseContext.remove();
            return reject(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录或登录已过期");
        }
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        BaseContext.remove();
    }

    private boolean reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), Result.error(status, message));
        return false;
    }

    private Long longClaim(Claims claims, String name) {
        Object value = claims.get(name);
        return value == null ? null : Long.valueOf(value.toString());
    }

    private Integer integerClaim(Claims claims, String name) {
        Object value = claims.get(name);
        return value == null ? null : Integer.valueOf(value.toString());
    }

    private String stringClaim(Claims claims, String name) {
        Object value = claims.get(name);
        return value == null ? null : value.toString();
    }

    private String requestPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }
}
