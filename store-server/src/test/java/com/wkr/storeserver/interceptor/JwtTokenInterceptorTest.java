package com.wkr.storeserver.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.common.JwtClaimsConstant;
import com.wkr.storecommon.properties.JwtProperties;
import com.wkr.storecommon.util.JwtUtils;
import com.wkr.storepojo.entity.SysUser;
import com.wkr.storepojo.enums.RoleEnum;
import com.wkr.storeserver.mapper.SysUserMapper;
import com.wkr.storeserver.security.RolePermissionChecker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * JWT 拦截器测试，验证有效令牌、无效身份、越权响应和用户上下文清理。
 */
class JwtTokenInterceptorTest {

    private static final String SECRET = "itcast";
    private JwtTokenInterceptor interceptor;
    private SysUserMapper sysUserMapper;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setAdminSecretKey(SECRET);
        properties.setAdminTtl(7_200_000L);
        properties.setAdminTokenName("token");
        sysUserMapper = mock(SysUserMapper.class);
        interceptor = new JwtTokenInterceptor(
                properties,
                new RolePermissionChecker(),
                sysUserMapper,
                new ObjectMapper());
    }

    @AfterEach
    void tearDown() {
        BaseContext.remove();
    }

    @Test
    void validStaffTokenCanAccessAppointmentApiAndPopulatesContext() throws Exception {
        stubCurrentUser(RoleEnum.STAFF, 1, 2001L);
        MockHttpServletRequest request = request("POST", "/admin/appointments", token(RoleEnum.STAFF));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals(1001L, BaseContext.getCurrentUserId());
        assertEquals(2001L, BaseContext.getCurrentId());
        assertEquals(RoleEnum.STAFF.getCode(), BaseContext.getCurrentRoleId());
        assertEquals(RoleEnum.STAFF.getRoleCode(), BaseContext.getCurrentRole());

        interceptor.afterCompletion(request, response, new Object(), null);
        assertNull(BaseContext.getCurrentUser());
    }

    @Test
    void readonlyWriteRequestReturnsForbidden() throws Exception {
        stubCurrentUser(RoleEnum.READONLY, 1, 2001L);
        MockHttpServletRequest request = request("POST", "/admin/customers", token(RoleEnum.READONLY));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString(StandardCharsets.UTF_8).contains("无权限访问该接口"));
        assertNull(BaseContext.getCurrentUser());
    }

    @Test
    void missingTokenReturnsUnauthorized() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/customers");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString(StandardCharsets.UTF_8).contains("未登录或登录已过期"));
    }

    @Test
    void mismatchedRoleIdAndRoleCodeReturnsUnauthorized() throws Exception {
        Map<String, Object> claims = claims(RoleEnum.STAFF);
        claims.put(JwtClaimsConstant.ROLE_ID, RoleEnum.FINANCE.getCode());
        String token = JwtUtils.createJWT(SECRET, 7_200_000L, claims);
        MockHttpServletRequest request = request("GET", "/admin/customers", token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
        assertNull(BaseContext.getCurrentUser());
    }

    @Test
    void disabledUserTokenReturnsUnauthorized() throws Exception {
        Map<String, Object> claims = claims(RoleEnum.SUPER_ADMIN);
        claims.put(JwtClaimsConstant.STATUS, 0);
        String token = JwtUtils.createJWT(SECRET, 7_200_000L, claims);
        MockHttpServletRequest request = request("GET", "/admin/users", token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
    }

    @Test
    void disabledDatabaseUserReturnsUnauthorized() throws Exception {
        stubCurrentUser(RoleEnum.SUPER_ADMIN, 0, 2001L);
        MockHttpServletRequest request = request("GET", "/admin/users", token(RoleEnum.SUPER_ADMIN));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
        assertNull(BaseContext.getCurrentUser());
    }

    @Test
    void changedDatabaseRoleRequiresRelogin() throws Exception {
        stubCurrentUser(RoleEnum.FINANCE, 1, 2001L);
        MockHttpServletRequest request = request("GET", "/admin/customers", token(RoleEnum.STAFF));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString(StandardCharsets.UTF_8).contains("登录信息已变更"));
    }

    private MockHttpServletRequest request(String method, String path, String token) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.addHeader("token", "Bearer " + token);
        return request;
    }

    private String token(RoleEnum role) {
        return JwtUtils.createJWT(SECRET, 7_200_000L, claims(role));
    }

    private Map<String, Object> claims(RoleEnum role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, 1001L);
        claims.put(JwtClaimsConstant.STAFF_ID, 2001L);
        claims.put(JwtClaimsConstant.USERNAME, "tester");
        claims.put(JwtClaimsConstant.STATUS, 1);
        claims.put(JwtClaimsConstant.ROLE_ID, role.getCode());
        claims.put(JwtClaimsConstant.ROLE_CODE, role.getRoleCode());
        return claims;
    }

    private void stubCurrentUser(RoleEnum role, Integer status, Long staffId) {
        SysUser sysUser = new SysUser();
        sysUser.setId(1001L);
        sysUser.setStaffId(staffId);
        sysUser.setRoleId(role.getCode());
        sysUser.setStatus(status);
        when(sysUserMapper.selectById(1001L)).thenReturn(sysUser);
    }
}
