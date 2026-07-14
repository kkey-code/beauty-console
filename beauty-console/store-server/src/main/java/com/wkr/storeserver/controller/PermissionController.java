package com.wkr.storeserver.controller;

import com.wkr.storecommon.common.Result;
import com.wkr.storepojo.vo.PermissionPointVO;
import com.wkr.storeserver.service.PermissionPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
