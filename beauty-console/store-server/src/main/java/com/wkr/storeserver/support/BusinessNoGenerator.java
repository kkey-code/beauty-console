package com.wkr.storeserver.support;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;

/**
 * Generates business numbers with a short module prefix and a globally unique id.
 */
public final class BusinessNoGenerator {

    private BusinessNoGenerator() {
    }

    public static String next(String prefix) {
        return prefix + IdWorker.getIdStr();
    }
}
