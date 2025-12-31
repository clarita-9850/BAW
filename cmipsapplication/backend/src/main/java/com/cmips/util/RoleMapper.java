package com.cmips.util;

import com.cmips.model.UserRole;

/**
 * Maps legacy or external role labels (including usernames or Keycloak subjects)
 * to the canonical CMIPS {@link UserRole} set.
 */
public final class RoleMapper {

    private RoleMapper() {}

    /**
     * Primary mapping entrypoint used throughout the application to translate
     * any raw role label/username into the canonical {@link UserRole}.
     */
    public static UserRole map(String rawRoleOrUsername) {
        return UserRole.from(rawRoleOrUsername);
    }

    public static UserRole fromRealmRole(String rawRole) {
        return UserRole.from(rawRole);
    }

    public static UserRole fromPreferredUsername(String preferredUsername) {
        return UserRole.from(preferredUsername);
    }

    public static String canonicalName(String rawRole) {
        return fromRealmRole(rawRole).name();
    }

    public static boolean isAdmin(String rawRole) {
        return fromRealmRole(rawRole) == UserRole.ADMIN;
    }

    public static boolean isSupervisor(String rawRole) {
        return fromRealmRole(rawRole) == UserRole.SUPERVISOR;
    }

    public static boolean isCaseWorker(String rawRole) {
        return fromRealmRole(rawRole) == UserRole.CASE_WORKER;
    }

    public static boolean isProvider(String rawRole) {
        return fromRealmRole(rawRole) == UserRole.PROVIDER;
    }

    public static boolean isRecipient(String rawRole) {
        return fromRealmRole(rawRole) == UserRole.RECIPIENT;
    }
}






