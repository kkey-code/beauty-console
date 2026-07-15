package com.wkr.storeserver.audit;

import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.common.ResultCode;
import com.wkr.storepojo.entity.OperationAuditLog;
import com.wkr.storeserver.mapper.OperationAuditLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
public class AuditLogAspect {

    private final OperationAuditLogMapper operationAuditLogMapper;

    public AuditLogAspect(OperationAuditLogMapper operationAuditLogMapper) {
        this.operationAuditLogMapper = operationAuditLogMapper;
    }

    @AfterReturning(pointcut = "@annotation(auditLog)", returning = "returnValue")
    public void record(JoinPoint joinPoint, AuditLog auditLog, Object returnValue) {
        if (returnValue instanceof Result<?> result
                && !Integer.valueOf(ResultCode.SUCCESS.getCode()).equals(result.getCode())) {
            return;
        }

        try {
            OperationAuditLog log = new OperationAuditLog();
            log.setOperatorUserId(BaseContext.getCurrentUserId());
            log.setOperatorStaffId(BaseContext.getCurrentId());
            log.setActionType(auditLog.action());
            log.setTargetType(auditLog.target());
            log.setTargetId(resolveTargetId(joinPoint, returnValue));
            log.setDetail(resolveDetail(auditLog, returnValue));
            log.setCreateTime(LocalDateTime.now());
            operationAuditLogMapper.insert(log);
        } catch (RuntimeException ex) {
            log.warn("Operation audit log write failed: action={}, target={}",
                    auditLog.action(), auditLog.target(), ex);
        }
    }

    private Long resolveTargetId(JoinPoint joinPoint, Object returnValue) {
        Long pathId = resolvePathVariableId(joinPoint);
        if (pathId != null) {
            return pathId;
        }
        if (returnValue instanceof Result<?> result && result.getData() instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private Long resolvePathVariableId(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Annotation[][] annotations = method.getParameterAnnotations();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < annotations.length; i++) {
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof PathVariable && args[i] instanceof Number number) {
                    return number.longValue();
                }
            }
        }
        return null;
    }

    private String resolveDetail(AuditLog auditLog, Object returnValue) {
        if (!auditLog.detail().isBlank()) {
            return auditLog.detail();
        }
        if (returnValue instanceof Result<?> result && result.getData() != null) {
            return String.valueOf(result.getData());
        }
        return null;
    }
}
