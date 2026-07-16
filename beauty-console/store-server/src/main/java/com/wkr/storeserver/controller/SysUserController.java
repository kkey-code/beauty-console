package com.wkr.storeserver.controller;

import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storecommon.exception.SystemException;
import com.wkr.storepojo.dto.SysUserDTO;
import com.wkr.storepojo.dto.SysUserLoginDTO;
import com.wkr.storepojo.dto.SysUserPageQueryDTO;
import com.wkr.storepojo.dto.SysUserStatusDTO;
import com.wkr.storepojo.dto.UserPermissionDTO;
import com.wkr.storepojo.entity.SysUser;
import com.wkr.storepojo.vo.LoginUserVO;
import com.wkr.storepojo.vo.SysUserVO;
import com.wkr.storepojo.vo.UserPermissionVO;
import com.wkr.storeserver.audit.AuditLog;
import com.wkr.storeserver.service.PermissionPointService;
import com.wkr.storeserver.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.time.LocalDateTime;

/**
 * 系统用户接口控制器，负责接收管理端请求、校验参数并调用服务层完成业务处理。
 */
@Slf4j
@RestController
@RequestMapping("/admin/users")
@Tag(name = "用户相关接口")
public class SysUserController {

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    private final PermissionPointService permissionPointService;

    public SysUserController(
            SysUserService sysUserService,
            PasswordEncoder passwordEncoder,
            PermissionPointService permissionPointService) {
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
        this.permissionPointService = permissionPointService;
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    public Result<LoginUserVO> login(@Valid @RequestBody SysUserLoginDTO sysUserLoginDTO) {
        return Result.success(sysUserService.login(sysUserLoginDTO));
    }

    @GetMapping
    @Operation(summary = "用户分页查询用户列表")
    public Result<PageResult<SysUserVO>> getList(@Valid SysUserPageQueryDTO sysUserPageQueryDTO) {
        return Result.success(sysUserService.page(sysUserPageQueryDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据用户 id 查询用户")
    public Result<SysUserVO> getById(@PathVariable("id") Long id) {
        return Result.success(sysUserService.getByID(id));
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "查询用户权限点")
    public Result<UserPermissionVO> getPermissions(@PathVariable("id") Long id) {
        return Result.success(permissionPointService.getUserPermissions(id));
    }

    @PutMapping("/{id}/permissions")
    @Operation(summary = "保存用户权限点")
    @AuditLog(action = "PERMISSIONS", target = "USER")
    public Result<?> updatePermissions(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserPermissionDTO userPermissionDTO) {
        permissionPointService.updateUserPermissions(id, userPermissionDTO);
        return Result.success();
    }

    @PostMapping
    @Operation(summary = "添加用户")
    @AuditLog(action = "CREATE", target = "USER")
    public Result<Long> add(@Valid @RequestBody SysUserDTO sysUserDTO) {
        sysUserService.validateStaffBinding(null, sysUserDTO.getRoleId(), sysUserDTO.getStaffId());
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(sysUserDTO, sysUser);
        String rawPassword = StringUtils.hasText(sysUserDTO.getPasswordHash())
                ? sysUserDTO.getPasswordHash()
                : SysUserService.DEFAULT_PASSWORD;
        sysUser.setPasswordHash(encodePasswordIfNecessary(rawPassword));
        sysUser.setCreateTime(LocalDateTime.now());
        sysUser.setUpdateTime(LocalDateTime.now());

        boolean saved = sysUserService.save(sysUser);
        if (!saved) {
            throw new SystemException("添加用户失败");
        }
        return Result.success(sysUser.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改用户")
    @AuditLog(action = "UPDATE", target = "USER")
    @Transactional
    public Result<?> update(@PathVariable("id") Long id, @Valid @RequestBody SysUserDTO sysUserDTO) {
        SysUser existing = sysUserService.getById(id);
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        sysUserService.validateStaffBinding(id, sysUserDTO.getRoleId(), sysUserDTO.getStaffId());
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(sysUserDTO, sysUser);
        sysUser.setId(id);
        sysUser.setPasswordHash(encodePasswordIfNecessary(sysUserDTO.getPasswordHash()));
        sysUser.setUpdateTime(LocalDateTime.now());

        boolean updated = sysUserService.updateById(sysUser);
        if (!updated) {
            throw new SystemException("修改用户失败");
        }
        if (!existing.getRoleId().equals(sysUserDTO.getRoleId())) {
            UserPermissionDTO roleDefault = new UserPermissionDTO();
            roleDefault.setPermissionCodes(List.of());
            roleDefault.setUseRoleDefault(true);
            permissionPointService.updateUserPermissions(id, roleDefault);
        }
        return Result.success();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "修改用户状态")
    @AuditLog(action = "STATUS", target = "USER")
    public Result<?> updateStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody(required = false) SysUserStatusDTO sysUserStatusDTO,
            @RequestParam(value = "status", required = false) Integer status) {
        Integer targetStatus = status != null
                ? status
                : sysUserStatusDTO == null ? null : sysUserStatusDTO.getStatus();
        sysUserService.updateStatus(id, targetStatus);
        return Result.success();
    }

    @PatchMapping("/{id}/reset-password")
    @Operation(summary = "将账号密码重置为默认密码123456")
    @AuditLog(action = "RESET_PASSWORD", target = "USER")
    public Result<?> resetPassword(@PathVariable("id") Long id) {
        sysUserService.resetPassword(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @AuditLog(action = "DELETE", target = "USER")
    public Result<?> deleteByid(@PathVariable("id") Long id) {
        if (id.equals(BaseContext.getCurrentUserId())) {
            throw new BusinessException("不能删除当前登录账号");
        }
        if (sysUserService.getById(id) == null) {
            throw new BusinessException("用户不存在");
        }
        boolean removed = sysUserService.removeById(id);
        if (!removed) {
            throw new BusinessException("删除用户失败");
        }
        return Result.success();
    }

    private String encodePasswordIfNecessary(String password) {
        if (!StringUtils.hasText(password)) {
            return null;
        }
        if (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$")) {
            return password;
        }
        return passwordEncoder.encode(password);
    }
}
