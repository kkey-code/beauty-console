package com.wkr.storeserver.service;

import com.wkr.storepojo.vo.DashboardOverviewVO;

/**
 * 工作台聚合查询服务。
 */
public interface DashboardService {

    DashboardOverviewVO getOverview(Long userId, Integer roleId);

    DashboardOverviewVO refreshOverview(Long userId, Integer roleId);
}
