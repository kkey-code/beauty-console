package com.wkr.storeserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storepojo.dto.SysUserLoginDTO;
import com.wkr.storepojo.dto.SysUserPageQueryDTO;
import com.wkr.storepojo.entity.SysUser;
import com.wkr.storepojo.vo.LoginUserVO;
import com.wkr.storepojo.vo.SysUserVO;

/**
* @author kkey
* @description 针对表【sys_user(用户账号表)】的数据库操作Service
* @createDate 2026-07-03 21:10:41
*/
public interface SysUserService extends IService<SysUser> {

    LoginUserVO login(SysUserLoginDTO sysUserLoginDTO);

    PageResult<SysUserVO> page(SysUserPageQueryDTO sysUserPageQueryDTO);

    SysUserVO getByID(Long id);

    void updateStatus(Long id, Integer status);
}
