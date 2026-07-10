package com.wkr.storeserver.security;

/**
 * Runtime API permission rule loaded from sys_permission.
 */
public record PermissionRule(String method, String pathPattern) {
}
