package com.wkr.storeserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storecommon.exception.SystemException;
import com.wkr.storepojo.dto.StaffMemberDTO;
import com.wkr.storepojo.dto.StaffMemberPageQueryDTO;
import com.wkr.storepojo.entity.StaffMember;
import com.wkr.storepojo.enums.CommonStatusEnum;
import com.wkr.storepojo.enums.GenderEnum;
import com.wkr.storepojo.vo.StaffMemberVO;
import com.wkr.storeserver.audit.AuditLog;
import com.wkr.storeserver.service.DeletionGuardService;
import com.wkr.storeserver.service.StaffMemberService;
import com.wkr.storeserver.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 员工档案接口控制器，负责接收管理端请求、校验参数并调用服务层完成业务处理。
 */
@Slf4j
@RestController
@RequestMapping("/admin/staff-members")
@Tag(name = "员工相关接口")
public class StaffMemberController {

    private final StaffMemberService staffMemberService;
    private final DeletionGuardService deletionGuardService;
    private final SysUserService sysUserService;

    public StaffMemberController(
            StaffMemberService staffMemberService,
            DeletionGuardService deletionGuardService,
            SysUserService sysUserService) {
        this.staffMemberService = staffMemberService;
        this.deletionGuardService = deletionGuardService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    @Operation(summary = "分页查询员工列表")
    public Result<PageResult<StaffMemberVO>> list(@Valid StaffMemberPageQueryDTO dto) {
        Page<StaffMember> pageQuery = new Page<>(dto.getPage(), dto.getPageSize());

        LambdaQueryWrapper<StaffMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getName()), StaffMember::getName, dto.getName())
                .like(StringUtils.hasText(dto.getPhone()), StaffMember::getPhone, dto.getPhone())
                .eq(dto.getStatus() != null, StaffMember::getStatus, dto.getStatus())
                .orderByDesc(StaffMember::getCreateTime)
                .orderByDesc(StaffMember::getId);

        Page<StaffMember> page = staffMemberService.page(pageQuery, wrapper);

        List<StaffMemberVO> voList = new ArrayList<>();
        for (StaffMember staffMember : page.getRecords()) {
            voList.add(toVO(staffMember));
        }

        PageResult<StaffMemberVO> result = new PageResult<>();
        result.setRecords(voList);
        result.setTotal(page.getTotal());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据员工 id 查询员工")
    public Result<StaffMemberVO> getById(@PathVariable("id") Long id) {
        StaffMember staffMember = staffMemberService.getById(id);
        if (staffMember == null) {
            throw new BusinessException("员工不存在");
        }
        return Result.success(toVO(staffMember));
    }

    @PostMapping
    @Operation(summary = "添加员工")
    @AuditLog(action = "CREATE", target = "STAFF")
    @Transactional
    public Result<Boolean> add(@Valid @RequestBody StaffMemberDTO staffMemberDTO) {
        StaffMember staffMember = new StaffMember();
        BeanUtils.copyProperties(staffMemberDTO, staffMember);
        staffMember.setCreateTime(LocalDateTime.now());
        staffMember.setUpdateTime(LocalDateTime.now());

        boolean saved = staffMemberService.save(staffMember);
        if (!saved) {
            throw new BusinessException("添加员工失败");
        }
        sysUserService.createForStaff(
                staffMember,
                staffMemberDTO.getAccountUsername(),
                staffMemberDTO.getAccountRoleId());
        return Result.success(true);
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改员工信息")
    @AuditLog(action = "UPDATE", target = "STAFF")
    public Result<Boolean> updateStaffMember(@PathVariable("id") Long id, @Valid @RequestBody StaffMemberDTO staffMemberDTO) {
        StaffMember staffMember = new StaffMember();
        BeanUtils.copyProperties(staffMemberDTO, staffMember);
        staffMember.setId(id);
        staffMember.setUpdateTime(LocalDateTime.now());

        boolean updated = staffMemberService.updateById(staffMember);
        if (!updated) {
            throw new SystemException("修改员工信息失败");
        }
        return Result.success(true);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "修改员工状态")
    @AuditLog(action = "STATUS", target = "STAFF")
    public Result<Boolean> updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status")
            @Min(value = 0, message = "状态只能是0或1")
            @Max(value = 1, message = "状态只能是0或1") Integer status) {
        StaffMember staffMember = new StaffMember();
        staffMember.setId(id);
        staffMember.setStatus(status);
        staffMember.setUpdateTime(LocalDateTime.now());

        boolean updated = staffMemberService.updateById(staffMember);
        if (!updated) {
            throw new SystemException("修改员工状态失败");
        }
        sysUserService.syncStatusForStaff(id, status);
        return Result.success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除员工")
    @AuditLog(action = "DELETE", target = "STAFF")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        deletionGuardService.assertStaffCanDelete(id);
        boolean removed = staffMemberService.removeById(id);
        if (!removed) {
            throw new SystemException("删除员工失败");
        }
        return Result.success(true);
    }

    private StaffMemberVO toVO(StaffMember staffMember) {
        StaffMemberVO staffMemberVO = new StaffMemberVO();
        BeanUtils.copyProperties(staffMember, staffMemberVO);
        staffMemberVO.setGenderName(GenderEnum.labelOf(staffMember.getGender()));
        staffMemberVO.setStatusName(CommonStatusEnum.labelOf(staffMember.getStatus()));
        return staffMemberVO;
    }
}
