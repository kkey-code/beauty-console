package com.wkr.storeserver.service;

import com.wkr.storecommon.common.JwtClaimsConstant;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storecommon.properties.JwtProperties;
import com.wkr.storecommon.util.JwtUtils;
import com.wkr.storepojo.dto.SysUserLoginDTO;
import com.wkr.storepojo.entity.SysUser;
import com.wkr.storepojo.entity.StaffMember;
import com.wkr.storepojo.enums.RoleEnum;
import com.wkr.storepojo.vo.LoginUserVO;
import com.wkr.storeserver.mapper.SysUserMapper;
import com.wkr.storeserver.service.impl.SysUserServiceImpl;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户登录服务测试，验证密码校验、登录时间更新、角色信息和 JWT 声明。
 */
@ExtendWith(MockitoExtension.class)
class SysUserServiceImplLoginTest {

    private static final String SECRET = "itcast";

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PermissionPointService permissionPointService;
    @Mock
    private StaffMemberService staffMemberService;

    private SysUserServiceImpl service;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setAdminSecretKey(SECRET);
        properties.setAdminTtl(7_200_000L);
        properties.setAdminTokenName("token");
        service = new SysUserServiceImpl(
                sysUserMapper,
                properties,
                passwordEncoder,
                permissionPointService,
                staffMemberService);
    }

    @Test
    void successfulLoginUpdatesTimestampAndCreatesRoleClaims() {
        LoginUserVO user = loginUser(RoleEnum.STAFF);
        when(sysUserMapper.loginByName("staff")).thenReturn(user);
        when(passwordEncoder.matches("123456", user.getPassword())).thenReturn(true);
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);
        when(permissionPointService.listEffectiveCodes(anyLong(), any(RoleEnum.class)))
                .thenReturn(List.of("dashboard:view", "appointments:view"));

        LoginUserVO result = service.login(loginDto("staff"));

        ArgumentCaptor<SysUser> updateCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).updateById(updateCaptor.capture());
        SysUser update = updateCaptor.getValue();
        assertNotNull(update.getLastLoginTime());
        assertNotNull(update.getUpdateTime());
        assertNull(update.getPasswordHash());

        assertNull(result.getPassword());
        assertEquals(RoleEnum.STAFF.getRoleCode(), result.getRoleCode());
        assertEquals(RoleEnum.STAFF.getDescription(), result.getRoleName());
        assertEquals(List.of("dashboard:view", "appointments:view"), result.getPermissions());

        Claims claims = JwtUtils.parseJWT(SECRET, result.getToken());
        assertEquals(RoleEnum.STAFF.getCode(), Integer.valueOf(claims.get(JwtClaimsConstant.ROLE_ID).toString()));
        assertEquals(RoleEnum.STAFF.getRoleCode(), claims.get(JwtClaimsConstant.ROLE_CODE));
    }

    @Test
    void unknownRoleIsRejectedBeforeTokenCreation() {
        LoginUserVO user = loginUser(null);
        user.setRoleId(99);
        when(sysUserMapper.loginByName("staff")).thenReturn(user);
        when(passwordEncoder.matches("123456", user.getPassword())).thenReturn(true);
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.login(loginDto("staff")));

        assertEquals("用户角色无效", exception.getMessage());
    }

    @Test
    void updateStatusRejectsInvalidStatus() {
        BusinessException nullStatusException = assertThrows(
                BusinessException.class,
                () -> service.updateStatus(1001L, null));
        assertEquals("用户状态不能为空", nullStatusException.getMessage());

        BusinessException invalidStatusException = assertThrows(
                BusinessException.class,
                () -> service.updateStatus(1001L, 2));
        assertEquals("用户状态只能是0或1", invalidStatusException.getMessage());
    }

    @Test
    void updateStatusRejectsMissingUser() {
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.updateStatus(1001L, 0));

        assertEquals("用户不存在或状态未更新", exception.getMessage());
    }

    @Test
    void updateStatusUpdatesStatusAndTimestamp() {
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        service.updateStatus(1001L, 0);

        ArgumentCaptor<SysUser> updateCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).updateById(updateCaptor.capture());
        SysUser update = updateCaptor.getValue();
        assertEquals(1001L, update.getId());
        assertEquals(0, update.getStatus());
        assertNotNull(update.getUpdateTime());
    }

    @Test
    void accountMustBindAnEmployee() {
        assertThrows(BusinessException.class, () -> service.validateStaffBinding(null, 3, null));
    }

    @Test
    void oneEmployeeCannotBindTwoAccounts() {
        StaffMember staffMember = new StaffMember();
        staffMember.setId(1007L);
        when(staffMemberService.getById(1007L)).thenReturn(staffMember);
        when(sysUserMapper.selectCount(any())).thenReturn(1L);

        assertThrows(BusinessException.class, () -> service.validateStaffBinding(null, 3, 1007L));
    }

    private LoginUserVO loginUser(RoleEnum role) {
        LoginUserVO user = new LoginUserVO();
        user.setId(1001L);
        user.setUsername("staff");
        user.setPassword("$2a$10$encoded");
        user.setRoleId(role == null ? null : role.getCode());
        user.setStaffId(2001L);
        user.setStatus(1);
        return user;
    }

    private SysUserLoginDTO loginDto(String username) {
        SysUserLoginDTO dto = new SysUserLoginDTO();
        dto.setUsername(username);
        dto.setPassword("123456");
        return dto;
    }
}
