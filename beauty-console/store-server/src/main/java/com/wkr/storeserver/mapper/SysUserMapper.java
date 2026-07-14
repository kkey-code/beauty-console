package com.wkr.storeserver.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wkr.storepojo.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wkr.storepojo.vo.LoginUserVO;
import com.wkr.storepojo.vo.SysUserVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

/**
* @author kkey
* @description 针对表【sys_user(用户账号表)】的数据库操作Mapper
* @createDate 2026-07-03 21:10:41
* @Entity com.wkr.storeserver.mapper / service / service.impl / controller.SysUser
*/
public interface SysUserMapper extends BaseMapper<SysUser> {

    LoginUserVO loginByName(@Param("username") String username);

    IPage<SysUserVO> selectUserPage(@Param("page") Page<SysUser> page, @Param("username") String username, @Param("status") Integer status, @Param("roleId") Integer roleId);

    SysUserVO getByID(@Param("id") Long id);
}




