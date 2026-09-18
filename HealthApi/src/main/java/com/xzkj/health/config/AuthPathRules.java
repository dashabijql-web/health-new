package com.xzkj.health.config;

public final class AuthPathRules {
    public static final String ADMIN_ROLE = "SUPER_ADMIN";

    private AuthPathRules() {
    }

    public static boolean requiresLogin(String path) {
        return path.startsWith("/department")
                || path.startsWith("/job-type")
                || path.startsWith("/employee")
                || path.startsWith("/device")
                || path.startsWith("/heart-rate")
                || "/auth/info".equals(path);
    }

    public static boolean requiresAdmin(String path, String method) {
        boolean managed = path.startsWith("/department")
                || path.startsWith("/job-type")
                || path.startsWith("/employee")
                || path.startsWith("/device")
                || path.startsWith("/heart-rate");
        if (!managed) {
            return false;
        }
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
    }
}
