package com.wkr.storeserver.security;

import java.io.Serializable;

/**
 * Runtime API permission rule loaded from sys_permission.
 */
public record PermissionRule(String method, String pathPattern) implements Serializable {
}
