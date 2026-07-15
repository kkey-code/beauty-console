package com.wkr.storeserver.controller;

import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.common.Result;
import com.wkr.storepojo.vo.DashboardOverviewVO;
import com.wkr.storeserver.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作台聚合接口，减少公网环境下的多次请求往返。
 */
@RestController
@RequestMapping("/admin/dashboard")
@Tag(name = "工作台接口")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    @Operation(summary = "获取工作台经营概览")
    public Result<DashboardOverviewVO> overview(
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
        Long userId = BaseContext.getCurrentUserId();
        Integer roleId = BaseContext.getCurrentRoleId();
        DashboardOverviewVO overview = refresh
                ? dashboardService.refreshOverview(userId, roleId)
                : dashboardService.getOverview(userId, roleId);
        return Result.success(overview);
    }
}
