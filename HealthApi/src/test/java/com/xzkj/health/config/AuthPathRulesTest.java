package com.xzkj.health.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthPathRulesTest {
    @Test
    void departmentListRequiresLoginButNotAdmin() {
        assertTrue(AuthPathRules.requiresLogin("/department/list"));
        assertFalse(AuthPathRules.requiresAdmin("/department/list", "GET"));
    }

    @Test
    void departmentWritesRequireAdmin() {
        assertTrue(AuthPathRules.requiresAdmin("/department/create", "POST"));
        assertTrue(AuthPathRules.requiresAdmin("/department/update", "PUT"));
        assertTrue(AuthPathRules.requiresAdmin("/department/delete/21", "DELETE"));
    }

    @Test
    void helloDoesNotRequireLogin() {
        assertFalse(AuthPathRules.requiresLogin("/hello"));
        assertFalse(AuthPathRules.requiresAdmin("/hello", "GET"));
    }
}
