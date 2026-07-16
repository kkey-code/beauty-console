package com.wkr.storeserver.controller;

import com.wkr.storecommon.common.Result;
import com.wkr.storepojo.vo.PermissionPointVO;
import com.wkr.storepojo.vo.RolePermissionVO;
import com.wkr.storepojo.dto.RolePermissionDTO;
import com.wkr.storeserver.audit.AuditLog;
import com.wkr.storeserver.service.PermissionPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/permissions")
@Tag(name = "权限点相关接口")
public class PermissionController {

    private final PermissionPointService permissionPointService;

    public PermissionController(PermissionPointService permissionPointService) {
        this.permissionPointService = permissionPointService;
    }

    @GetMapping
    @Operation(summary = "查询全部权限点")
    public Result<List<PermissionPointVO>> list() {
        return Result.success(permissionPointService.listPermissionPoints());
    }

    @GetMapping("/roles/{roleId}")
    @Operation(summary = "查询角色默认权限")
    public Result<RolePermissionVO> getRolePermissions(@PathVariable("roleId") Integer roleId) {
        return Result.success(permissionPointService.getRolePermissions(roleId));
    }

    @PutMapping("/roles/{roleId}")
    @Operation(summary = "保存角色默认权限")
    @AuditLog(action = "ROLE_PERMISSIONS", target = "ROLE")
    public Result<?> updateRolePermissions(
            @PathVariable("roleId") Integer roleId,
            @Valid @RequestBody RolePermissionDTO dto) {
        permissionPointService.updateRolePermissions(roleId, dto);
        return Result.success();
    }
}
