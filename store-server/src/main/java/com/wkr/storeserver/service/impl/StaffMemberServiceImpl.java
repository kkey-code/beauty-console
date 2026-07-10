package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storepojo.entity.StaffMember;
import com.wkr.storeserver.service.StaffMemberService;
import com.wkr.storeserver.mapper.StaffMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author kkey
* @description 针对表【staff_member(员工表)】的数据库操作Service实现
* @createDate 2026-07-03 21:10:41
*/
@Service
public class StaffMemberServiceImpl extends ServiceImpl<StaffMemberMapper, StaffMember> implements StaffMemberService{

}




