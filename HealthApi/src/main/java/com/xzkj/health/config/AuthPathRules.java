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
                || path.startsWith("/blood-pressure")
                || path.startsWith("/blood-oxygen")
                || path.startsWith("/temperature")
                || path.startsWith("/pressure")
                || path.startsWith("/sleep")
                || path.startsWith("/alert-config")
                || "/auth/info".equals(path);
    }

    public static boolean requiresAdmin(String path, String method) {
        if ("/alert-config/evaluate".equals(path) && "POST".equalsIgnoreCase(method)) {
            return false;
        }
        boolean managed = path.startsWith("/department")
                || path.startsWith("/job-type")
                || path.startsWith("/employee")
                || path.startsWith("/device")
                || path.startsWith("/heart-rate")
                || path.startsWith("/blood-pressure")
                || path.startsWith("/blood-oxygen")
                || path.startsWith("/temperature")
                || path.startsWith("/pressure")
                || path.startsWith("/sleep")
                || path.startsWith("/alert-config");
        if (!managed) {
            return false;
        }
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
    }
}
