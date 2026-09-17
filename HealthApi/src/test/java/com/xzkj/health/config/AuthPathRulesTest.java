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
    void jobTypeListRequiresLoginButNotAdmin() {
        assertTrue(AuthPathRules.requiresLogin("/job-type/list"));
        assertFalse(AuthPathRules.requiresAdmin("/job-type/list", "GET"));
    }

    @Test
    void jobTypeWritesRequireAdmin() {
        assertTrue(AuthPathRules.requiresAdmin("/job-type/create", "POST"));
        assertTrue(AuthPathRules.requiresAdmin("/job-type/update", "PUT"));
        assertTrue(AuthPathRules.requiresAdmin("/job-type/delete/1", "DELETE"));
    }

    @Test
    void employeeListRequiresLoginButNotAdmin() {
        assertTrue(AuthPathRules.requiresLogin("/employee/list"));
        assertFalse(AuthPathRules.requiresAdmin("/employee/list", "GET"));
    }

    @Test
    void employeeWritesRequireAdmin() {
        assertTrue(AuthPathRules.requiresAdmin("/employee/create", "POST"));
        assertTrue(AuthPathRules.requiresAdmin("/employee/update", "PUT"));
        assertTrue(AuthPathRules.requiresAdmin("/employee/delete/1", "DELETE"));
    }

    @Test
    void deviceListRequiresLoginButNotAdmin() {
        assertTrue(AuthPathRules.requiresLogin("/device/list"));
        assertFalse(AuthPathRules.requiresAdmin("/device/list", "GET"));
    }

    @Test
    void helloDoesNotRequireLogin() {
        assertFalse(AuthPathRules.requiresLogin("/hello"));
        assertFalse(AuthPathRules.requiresAdmin("/hello", "GET"));
    }
}
