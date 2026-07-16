package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storecommon.common.JwtClaimsConstant;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storecommon.properties.JwtProperties;
import com.wkr.storecommon.util.JwtUtils;
import com.wkr.storepojo.dto.SysUserLoginDTO;
import com.wkr.storepojo.dto.SysUserPageQueryDTO;
import com.wkr.storepojo.entity.SysUser;
import com.wkr.storepojo.entity.StaffMember;
import com.wkr.storepojo.enums.RoleEnum;
import com.wkr.storepojo.vo.LoginUserVO;
import com.wkr.storepojo.vo.SysUserVO;
import com.wkr.storeserver.mapper.SysUserMapper;
import com.wkr.storeserver.service.PermissionPointService;
import com.wkr.storeserver.service.StaffMemberService;
import com.wkr.storeserver.service.SysUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 系统用户服务实现，封装该业务模块的查询、校验、状态更新和持久化流程。
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final PermissionPointService permissionPointService;
    private final StaffMemberService staffMemberService;

    public SysUserServiceImpl(
            SysUserMapper sysUserMapper,
            JwtProperties jwtProperties,
            PasswordEncoder passwordEncoder,
            PermissionPointService permissionPointService,
            StaffMemberService staffMemberService) {
        this.sysUserMapper = sysUserMapper;
        this.jwtProperties = jwtProperties;
        this.passwordEncoder = passwordEncoder;
        this.permissionPointService = permissionPointService;
        this.staffMemberService = staffMemberService;
    }

    @Override
    public LoginUserVO login(SysUserLoginDTO sysUserLoginDTO) {
        LoginUserVO loginUserVO = sysUserMapper.loginByName(sysUserLoginDTO.getUsername());
        if (loginUserVO == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (loginUserVO.getStatus() == null || loginUserVO.getStatus() == 0) {
            throw new BusinessException("用户已禁用");
        }
        if (!matchesAndUpgradePassword(sysUserLoginDTO.getPassword(), loginUserVO)) {
            throw new BusinessException("用户名或密码错误");
        }

        fillLoginUserDisplayFields(loginUserVO);
        loginUserVO.setPermissions(permissionPointService.listEffectiveCodes(
                loginUserVO.getId(),
                RoleEnum.fromCode(loginUserVO.getRoleId())));

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USERNAME, loginUserVO.getUsername());
        claims.put(JwtClaimsConstant.STATUS, loginUserVO.getStatus());
        claims.put(JwtClaimsConstant.STAFF_ID, loginUserVO.getStaffId());
        claims.put(JwtClaimsConstant.USER_ID, loginUserVO.getId());
        claims.put(JwtClaimsConstant.ROLE_ID, loginUserVO.getRoleId());
        claims.put(JwtClaimsConstant.ROLE_CODE, loginUserVO.getRoleCode());

        String token = JwtUtils.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        loginUserVO.setToken(token);
        loginUserVO.setPassword(null);
        return loginUserVO;
    }

    @Override
    public PageResult<SysUserVO> page(SysUserPageQueryDTO sysUserPageQueryDTO) {
        Page<SysUser> page = new Page<>(sysUserPageQueryDTO.getPage(), sysUserPageQueryDTO.getPageSize());

        IPage<SysUserVO> records = sysUserMapper.selectUserPage(
                page,
                sysUserPageQueryDTO.getUsername(),
                sysUserPageQueryDTO.getStatus(),
                sysUserPageQueryDTO.getRoleId());

        for (SysUserVO record : records.getRecords()) {
            fillUserDisplayFields(record);
        }

        return PageResult.<SysUserVO>builder()
                .total(records.getTotal())
                .records(records.getRecords())
                .build();
    }

    @Override
    public SysUserVO getByID(Long id) {
        SysUserVO sysUserVO = sysUserMapper.getByID(id);
        if (sysUserVO == null) {
            throw new BusinessException("用户不存在");
        }
        fillUserDisplayFields(sysUserVO);
        return sysUserVO;
    }

    @Override
    public void validateStaffBinding(Long userId, Integer roleId, Long staffId) {
        if (RoleEnum.fromCode(roleId) == null) {
            throw new BusinessException("用户角色无效");
        }
        if (staffId == null) {
            throw new BusinessException("每个账号必须关联一名员工");
        }
        if (staffMemberService.getById(staffId) == null) {
            throw new BusinessException("关联员工不存在");
        }
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStaffId, staffId);
        if (userId != null) {
            wrapper.ne(SysUser::getId, userId);
        }
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该员工已有账号，一个员工只能关联一个账号");
        }
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (id == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (status == null) {
            throw new BusinessException("用户状态不能为空");
        }
        if (!status.equals(0) && !status.equals(1)) {
            throw new BusinessException("用户状态只能是0或1");
        }

        SysUser sysUser = new SysUser();
        sysUser.setStatus(status);
        sysUser.setId(id);
        sysUser.setUpdateTime(LocalDateTime.now());

        int updated = sysUserMapper.updateById(sysUser);
        if (updated != 1) {
            throw new BusinessException("用户不存在或状态未更新");
        }
    }

    @Override
    @Transactional
    public Long createForStaff(StaffMember staffMember, String username, Integer roleId) {
        if (staffMember == null || staffMember.getId() == null) {
            throw new BusinessException("员工信息不存在，无法创建账号");
        }
        Integer normalizedRoleId = roleId == null ? RoleEnum.STAFF.getCode() : roleId;
        validateStaffBinding(null, normalizedRoleId, staffMember.getId());

        String normalizedUsername = StringUtils.hasText(username)
                ? username.trim()
                : defaultUsername(staffMember.getId());
        if (sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, normalizedUsername)) > 0) {
            throw new BusinessException("登录账号已存在，请更换账号");
        }

        SysUser user = new SysUser();
        user.setUsername(normalizedUsername);
        user.setPasswordHash(passwordEncoder.encode(SysUserService.DEFAULT_PASSWORD));
        user.setRoleId(normalizedRoleId);
        user.setStaffId(staffMember.getId());
        user.setStatus(staffMember.getStatus() == null ? 1 : staffMember.getStatus());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        if (sysUserMapper.insert(user) != 1) {
            throw new BusinessException("员工账号创建失败");
        }
        return user.getId();
    }

    @Override
    @Transactional
    public void ensureAccountsForAllStaff() {
        List<StaffMember> staffMembers = staffMemberService.list();
        if (staffMembers == null || staffMembers.isEmpty()) {
            return;
        }

        Set<Long> boundStaffIds = new HashSet<>();
        Set<String> usernames = new HashSet<>();
        for (SysUser user : sysUserMapper.selectList(null)) {
            if (user.getStaffId() != null) {
                boundStaffIds.add(user.getStaffId());
            }
            if (StringUtils.hasText(user.getUsername())) {
                usernames.add(user.getUsername());
            }
        }

        String encodedDefaultPassword = passwordEncoder.encode(SysUserService.DEFAULT_PASSWORD);
        LocalDateTime now = LocalDateTime.now();
        for (StaffMember staffMember : staffMembers) {
            if (staffMember.getId() == null || boundStaffIds.contains(staffMember.getId())) {
                continue;
            }
            String username = uniqueDefaultUsername(staffMember.getId(), usernames);
            SysUser user = new SysUser();
            user.setUsername(username);
            user.setPasswordHash(encodedDefaultPassword);
            user.setRoleId(RoleEnum.STAFF.getCode());
            user.setStaffId(staffMember.getId());
            user.setStatus(staffMember.getStatus() == null ? 1 : staffMember.getStatus());
            user.setCreateTime(now);
            user.setUpdateTime(now);
            if (sysUserMapper.insert(user) != 1) {
                throw new BusinessException("为员工 " + staffMember.getName() + " 自动创建账号失败");
            }
            boundStaffIds.add(staffMember.getId());
            usernames.add(username);
        }
    }

    @Override
    public void syncStatusForStaff(Long staffId, Integer status) {
        if (staffId == null || status == null) {
            return;
        }
        SysUser update = new SysUser();
        update.setStatus(status);
        update.setUpdateTime(LocalDateTime.now());
        sysUserMapper.update(update, new LambdaQueryWrapper<SysUser>().eq(SysUser::getStaffId, staffId));
    }

    @Override
    public void resetPassword(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPasswordHash(passwordEncoder.encode(SysUserService.DEFAULT_PASSWORD));
        update.setUpdateTime(LocalDateTime.now());
        if (sysUserMapper.updateById(update) != 1) {
            throw new BusinessException("密码重置失败");
        }
    }

    private String defaultUsername(Long staffId) {
        return "emp" + staffId;
    }

    private String uniqueDefaultUsername(Long staffId, Set<String> usernames) {
        String base = defaultUsername(staffId);
        if (!usernames.contains(base)) {
            return base;
        }
        int suffix = 1;
        while (usernames.contains(base + "_" + suffix)) {
            suffix++;
        }
        return base + "_" + suffix;
    }

    private boolean matchesAndUpgradePassword(String rawPassword, LoginUserVO loginUserVO) {
        String storedPassword = loginUserVO.getPassword();
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(storedPassword)) {
            return false;
        }

        if (isBcryptPassword(storedPassword)) {
            boolean matched = passwordEncoder.matches(rawPassword, storedPassword);
            if (matched) {
                updateLoginState(loginUserVO.getId(), null);
            }
            return matched;
        }

        if (!rawPassword.equals(storedPassword)) {
            return false;
        }

        updateLoginState(loginUserVO.getId(), passwordEncoder.encode(rawPassword));
        return true;
    }

    private void updateLoginState(Long userId, String encodedPassword) {
        LocalDateTime now = LocalDateTime.now();
        SysUser sysUser = new SysUser();
        sysUser.setId(userId);
        sysUser.setLastLoginTime(now);
        sysUser.setUpdateTime(now);
        if (encodedPassword != null) {
            sysUser.setPasswordHash(encodedPassword);
        }
        sysUserMapper.updateById(sysUser);
    }

    private boolean isBcryptPassword(String password) {
        return password.startsWith("$2a$")
                || password.startsWith("$2b$")
                || password.startsWith("$2y$");
    }

    private void fillUserDisplayFields(SysUserVO sysUserVO) {
        if (sysUserVO == null) {
            return;
        }
        sysUserVO.setRoleCode(RoleEnum.codeOf(sysUserVO.getRoleId()));
        sysUserVO.setRoleName(RoleEnum.labelOf(sysUserVO.getRoleId()));
        sysUserVO.setStatusName(statusNameOf(sysUserVO.getStatus()));
    }

    private void fillLoginUserDisplayFields(LoginUserVO loginUserVO) {
        if (loginUserVO == null) {
            return;
        }
        RoleEnum role = RoleEnum.fromCode(loginUserVO.getRoleId());
        if (role == null) {
            throw new BusinessException("用户角色无效");
        }
        loginUserVO.setRoleCode(role.getRoleCode());
        loginUserVO.setRoleName(role.getDescription());
    }

    private String statusNameOf(Integer status) {
        if (status == null) {
            return null;
        }
        if (status.equals(1)) {
            return "启用";
        }
        if (status.equals(0)) {
            return "禁用";
        }
        return "未知";
    }
}
