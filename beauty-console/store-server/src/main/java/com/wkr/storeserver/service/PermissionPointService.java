package com.wkr.storeserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkr.storepojo.dto.UserPermissionDTO;
import com.wkr.storepojo.dto.RolePermissionDTO;
import com.wkr.storepojo.entity.SysPermission;
import com.wkr.storepojo.enums.RoleEnum;
import com.wkr.storepojo.vo.PermissionPointVO;
import com.wkr.storepojo.vo.UserPermissionVO;
import com.wkr.storepojo.vo.RolePermissionVO;
import com.wkr.storeserver.security.PermissionRule;

import java.util.List;

public interface PermissionPointService extends IService<SysPermission> {

    boolean isPermissionModelReady();

    List<String> listEffectiveCodes(Long userId, RoleEnum role);

    List<PermissionRule> listEffectiveRules(Long userId, RoleEnum role);

    List<PermissionPointVO> listPermissionPoints();

    UserPermissionVO getUserPermissions(Long userId);

    void updateUserPermissions(Long userId, UserPermissionDTO dto);

    RolePermissionVO getRolePermissions(Integer roleId);

    void updateRolePermissions(Integer roleId, RolePermissionDTO dto);
}
