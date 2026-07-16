package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.UserPermissionDTO;
import com.wkr.storepojo.dto.RolePermissionDTO;
import com.wkr.storepojo.entity.SysPermission;
import com.wkr.storepojo.entity.SysRolePermission;
import com.wkr.storepojo.entity.SysUser;
import com.wkr.storepojo.entity.SysUserPermission;
import com.wkr.storepojo.enums.RoleEnum;
import com.wkr.storepojo.vo.PermissionPointVO;
import com.wkr.storepojo.vo.UserPermissionVO;
import com.wkr.storepojo.vo.RolePermissionVO;
import com.wkr.storeserver.mapper.SysPermissionMapper;
import com.wkr.storeserver.mapper.SysRolePermissionMapper;
import com.wkr.storeserver.mapper.SysUserMapper;
import com.wkr.storeserver.mapper.SysUserPermissionMapper;
import com.wkr.storeserver.security.PermissionRule;
import com.wkr.storeserver.security.RolePermissionPolicy;
import com.wkr.storeserver.service.PermissionPointService;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionPointServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission>
        implements PermissionPointService {

    private static final int ENABLED = 1;
    private static final long EMPTY_OVERRIDE_PERMISSION_ID = 0L;

    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysUserPermissionMapper sysUserPermissionMapper;
    private final SysUserMapper sysUserMapper;

    public PermissionPointServiceImpl(
            SysPermissionMapper sysPermissionMapper,
            SysRolePermissionMapper sysRolePermissionMapper,
            SysUserPermissionMapper sysUserPermissionMapper,
            SysUserMapper sysUserMapper) {
        this.sysPermissionMapper = sysPermissionMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.sysUserPermissionMapper = sysUserPermissionMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    @Cacheable(cacheNames = "permission:model", key = "'ready'", unless = "#result == false")
    public boolean isPermissionModelReady() {
        try {
            return sysPermissionMapper.selectCount(activePermissionWrapper()) > 0;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    @Override
    @Cacheable(
            cacheNames = "permission:codes",
            key = "#p0 + ':' + (#p1 == null ? 'NONE' : #p1.name())",
            sync = true)
    public List<String> listEffectiveCodes(Long userId, RoleEnum role) {
        try {
            return listEffectivePermissions(userId, role).stream()
                    .map(SysPermission::getPermissionCode)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (RuntimeException ex) {
            return List.of();
        }
    }

    @Override
    @Cacheable(
            cacheNames = "permission:rules",
            key = "#p0 + ':' + (#p1 == null ? 'NONE' : #p1.name())",
            sync = true)
    public List<PermissionRule> listEffectiveRules(Long userId, RoleEnum role) {
        return listEffectivePermissions(userId, role).stream()
                .filter(item -> StringUtils.hasText(item.getMethod()) && StringUtils.hasText(item.getPathPattern()))
                .map(item -> new PermissionRule(item.getMethod(), item.getPathPattern()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionPointVO> listPermissionPoints() {
        return listAllActivePermissions().stream()
                .map(this::toPermissionPointVO)
                .collect(Collectors.toList());
    }

    @Override
    public UserPermissionVO getUserPermissions(Long userId) {
        SysUser user = requireUser(userId);
        RoleEnum role = RoleEnum.fromCode(user.getRoleId());

        UserPermissionVO vo = new UserPermissionVO();
        vo.setUserId(userId);
        vo.setCustomized(hasUserPermissionOverride(userId));
        vo.setPermissionCodes(listEffectiveCodes(userId, role));
        vo.setRolePermissionCodes(listRoleDefaultCodes(role));
        vo.setAllPermissions(listAssignablePermissionPoints(role));
        return vo;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "permission:codes", allEntries = true),
            @CacheEvict(cacheNames = "permission:rules", allEntries = true),
            @CacheEvict(cacheNames = "dashboard:overview", allEntries = true)
    })
    public void updateUserPermissions(Long userId, UserPermissionDTO dto) {
        SysUser user = requireUser(userId);
        RoleEnum role = RoleEnum.fromCode(user.getRoleId());
        if (dto == null || dto.getPermissionCodes() == null) {
            throw new BusinessException("权限点不能为空");
        }

        sysUserPermissionMapper.delete(new LambdaQueryWrapper<SysUserPermission>()
                .eq(SysUserPermission::getUserId, userId));

        if (Boolean.TRUE.equals(dto.getUseRoleDefault())) {
            return;
        }

        Set<String> permissionCodes = dto.getPermissionCodes().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        assertAssignable(role, permissionCodes);
        if (permissionCodes.isEmpty()) {
            saveUserPermission(userId, EMPTY_OVERRIDE_PERMISSION_ID, LocalDateTime.now());
            return;
        }

        List<SysPermission> permissions = sysPermissionMapper.selectList(
                activePermissionWrapper().in(SysPermission::getPermissionCode, permissionCodes));
        if (permissions.size() != permissionCodes.size()) {
            throw new BusinessException("权限点不存在或已停用");
        }

        LocalDateTime now = LocalDateTime.now();
        for (SysPermission permission : permissions) {
            saveUserPermission(userId, permission.getId(), now);
        }
    }

    @Override
    public RolePermissionVO getRolePermissions(Integer roleId) {
        RoleEnum role = requireRole(roleId);
        RolePermissionVO vo = new RolePermissionVO();
        vo.setRoleId(role.getCode());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getDescription());
        vo.setPermissionCodes(listRoleDefaultCodes(role));
        vo.setAllPermissions(listAssignablePermissionPoints(role));
        return vo;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "permission:codes", allEntries = true),
            @CacheEvict(cacheNames = "permission:rules", allEntries = true),
            @CacheEvict(cacheNames = "dashboard:overview", allEntries = true)
    })
    public void updateRolePermissions(Integer roleId, RolePermissionDTO dto) {
        RoleEnum role = requireRole(roleId);
        if (role == RoleEnum.SUPER_ADMIN) {
            throw new BusinessException("超级管理员始终拥有全部权限，无需配置");
        }
        if (dto == null || dto.getPermissionCodes() == null) {
            throw new BusinessException("权限点不能为空");
        }
        Set<String> permissionCodes = dto.getPermissionCodes().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        assertAssignable(role, permissionCodes);

        List<SysPermission> permissions = permissionCodes.isEmpty()
                ? List.of()
                : sysPermissionMapper.selectList(
                        activePermissionWrapper().in(SysPermission::getPermissionCode, permissionCodes));
        if (permissions.size() != permissionCodes.size()) {
            throw new BusinessException("权限点不存在或已停用");
        }

        sysRolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, role.getCode()));
        LocalDateTime now = LocalDateTime.now();
        for (SysPermission permission : permissions) {
            SysRolePermission relation = new SysRolePermission();
            relation.setRoleId(role.getCode());
            relation.setPermissionId(permission.getId());
            relation.setCreateTime(now);
            sysRolePermissionMapper.insert(relation);
        }
    }

    private List<SysPermission> listEffectivePermissions(Long userId, RoleEnum role) {
        if (role == null) {
            return List.of();
        }
        List<SysPermission> permissions;
        if (role == RoleEnum.SUPER_ADMIN) {
            permissions = listAllActivePermissions();
        } else if (userId != null && hasUserPermissionOverride(userId)) {
            permissions = listUserPermissions(userId);
        } else {
            permissions = listRolePermissions(role);
        }
        return permissions.stream()
                .filter(permission -> RolePermissionPolicy.isAllowed(role, permission.getPermissionCode()))
                .collect(Collectors.toList());
    }

    private List<String> listRoleDefaultCodes(RoleEnum role) {
        if (role == null) {
            return List.of();
        }
        List<SysPermission> permissions = role == RoleEnum.SUPER_ADMIN
                ? listAllActivePermissions()
                : listRolePermissions(role);
        return permissions.stream()
                .filter(permission -> RolePermissionPolicy.isAllowed(role, permission.getPermissionCode()))
                .map(SysPermission::getPermissionCode)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<PermissionPointVO> listAssignablePermissionPoints(RoleEnum role) {
        return listAllActivePermissions().stream()
                .filter(permission -> RolePermissionPolicy.isAllowed(role, permission.getPermissionCode()))
                .map(this::toPermissionPointVO)
                .collect(Collectors.toList());
    }

    private void assertAssignable(RoleEnum role, Set<String> permissionCodes) {
        String forbidden = permissionCodes.stream()
                .filter(code -> !RolePermissionPolicy.isAllowed(role, code))
                .findFirst()
                .orElse(null);
        if (forbidden != null) {
            throw new BusinessException("当前角色不能分配权限：" + forbidden);
        }
    }

    private RoleEnum requireRole(Integer roleId) {
        RoleEnum role = RoleEnum.fromCode(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return role;
    }

    private boolean hasUserPermissionOverride(Long userId) {
        if (userId == null) {
            return false;
        }
        return sysUserPermissionMapper.selectCount(new LambdaQueryWrapper<SysUserPermission>()
                .eq(SysUserPermission::getUserId, userId)) > 0;
    }

    private List<SysPermission> listAllActivePermissions() {
        return sysPermissionMapper.selectList(activePermissionWrapper()
                .orderByAsc(SysPermission::getPermissionGroup)
                .orderByAsc(SysPermission::getId));
    }

    private List<SysPermission> listUserPermissions(Long userId) {
        List<SysUserPermission> relations = sysUserPermissionMapper.selectList(
                new LambdaQueryWrapper<SysUserPermission>().eq(SysUserPermission::getUserId, userId));
        return listPermissionsByIds(relations.stream()
                .map(SysUserPermission::getPermissionId)
                .collect(Collectors.toList()));
    }

    private List<SysPermission> listRolePermissions(RoleEnum role) {
        List<SysRolePermission> relations = sysRolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, role.getCode()));
        return listPermissionsByIds(relations.stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList()));
    }

    private List<SysPermission> listPermissionsByIds(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return new ArrayList<>();
        }
        return sysPermissionMapper.selectList(activePermissionWrapper()
                .in(SysPermission::getId, permissionIds)
                .orderByAsc(SysPermission::getPermissionGroup)
                .orderByAsc(SysPermission::getId));
    }

    private LambdaQueryWrapper<SysPermission> activePermissionWrapper() {
        return new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getStatus, ENABLED);
    }

    private SysUser requireUser(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private void saveUserPermission(Long userId, Long permissionId, LocalDateTime createTime) {
        SysUserPermission userPermission = new SysUserPermission();
        userPermission.setUserId(userId);
        userPermission.setPermissionId(permissionId);
        userPermission.setCreateTime(createTime);
        sysUserPermissionMapper.insert(userPermission);
    }

    private PermissionPointVO toPermissionPointVO(SysPermission permission) {
        PermissionPointVO vo = new PermissionPointVO();
        BeanUtils.copyProperties(permission, vo);
        return vo;
    }
}
