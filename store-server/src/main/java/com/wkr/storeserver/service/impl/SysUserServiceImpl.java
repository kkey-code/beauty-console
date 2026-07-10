package com.wkr.storeserver.service.impl;

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
import com.wkr.storepojo.enums.RoleEnum;
import com.wkr.storepojo.vo.LoginUserVO;
import com.wkr.storepojo.vo.SysUserVO;
import com.wkr.storeserver.mapper.SysUserMapper;
import com.wkr.storeserver.service.SysUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统用户服务实现，封装该业务模块的查询、校验、状态更新和持久化流程。
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;

    public SysUserServiceImpl(
            SysUserMapper sysUserMapper,
            JwtProperties jwtProperties,
            PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.jwtProperties = jwtProperties;
        this.passwordEncoder = passwordEncoder;
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
