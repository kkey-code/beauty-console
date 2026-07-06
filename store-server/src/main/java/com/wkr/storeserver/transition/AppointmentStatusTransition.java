package com.wkr.storeserver.transition;

import com.wkr.storecommon.exception.IllegalStatusTransitionException;
import com.wkr.storepojo.enums.AppointmentEventEnum;
import com.wkr.storepojo.enums.AppointmentStatusEnum;

import java.util.HashMap;
import java.util.Map;

public class AppointmentStatusTransition {

    private static final Map<AppointmentStatusEnum, Map<AppointmentEventEnum, AppointmentStatusEnum>> RULES = new HashMap<>();

    static {
        // 待确认 + 确认 -> 已确认
        addRule(AppointmentStatusEnum.PENDING, AppointmentEventEnum.CONFIRM, AppointmentStatusEnum.CONFIRMED);
        // 待确认 + 取消 -> 已取消
        addRule(AppointmentStatusEnum.PENDING, AppointmentEventEnum.CANCEL, AppointmentStatusEnum.CANCELED);
        // 已确认 + 完成 -> 已完成
        addRule(AppointmentStatusEnum.CONFIRMED, AppointmentEventEnum.COMPLETE, AppointmentStatusEnum.COMPLETED);
        // 已确认 + 取消 -> 已取消
        addRule(AppointmentStatusEnum.CONFIRMED, AppointmentEventEnum.CANCEL, AppointmentStatusEnum.CANCELED);
    }

    private static void addRule(AppointmentStatusEnum current, AppointmentEventEnum event, AppointmentStatusEnum next) {
        RULES.computeIfAbsent(current, k -> new HashMap<>()).put(event, next);
    }

    /**
     * 根据当前状态和事件获取下一个状态，找不到说明非法
     */
    public static AppointmentStatusEnum next(AppointmentStatusEnum current, AppointmentEventEnum event) {
        Map<AppointmentEventEnum, AppointmentStatusEnum> eventMap = RULES.get(current);
        if (eventMap == null || !eventMap.containsKey(event)) {
            throw new IllegalStatusTransitionException(
                    "预约在[" + current.getLabel() + "]状态下不允许执行[" + event.getLabel() + "]操作"
            );
        }
        return eventMap.get(event);
    }
}
