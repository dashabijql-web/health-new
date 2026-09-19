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
    void deviceWritesRequireAdmin() {
        assertTrue(AuthPathRules.requiresAdmin("/device/create", "POST"));
        assertTrue(AuthPathRules.requiresAdmin("/device/update", "PUT"));
        assertTrue(AuthPathRules.requiresAdmin("/device/delete/1", "DELETE"));
    }

    @Test
    void heartRateListRequiresLoginButNotAdmin() {
        assertTrue(AuthPathRules.requiresLogin("/heart-rate/list"));
        assertFalse(AuthPathRules.requiresAdmin("/heart-rate/list", "GET"));
        assertTrue(AuthPathRules.requiresAdmin("/heart-rate/create", "POST"));
    }

    @Test
    void bloodPressureListRequiresLoginButNotAdmin() {
        assertTrue(AuthPathRules.requiresLogin("/blood-pressure/list"));
        assertFalse(AuthPathRules.requiresAdmin("/blood-pressure/list", "GET"));
        assertTrue(AuthPathRules.requiresAdmin("/blood-pressure/create", "POST"));
    }

    @Test
    void bloodOxygenListRequiresLoginButNotAdmin() {
        assertTrue(AuthPathRules.requiresLogin("/blood-oxygen/list"));
        assertFalse(AuthPathRules.requiresAdmin("/blood-oxygen/list", "GET"));
        assertTrue(AuthPathRules.requiresAdmin("/blood-oxygen/create", "POST"));
    }

    @Test
    void temperatureListRequiresLoginButNotAdmin() {
        assertTrue(AuthPathRules.requiresLogin("/temperature/list"));
        assertFalse(AuthPathRules.requiresAdmin("/temperature/list", "GET"));
        assertTrue(AuthPathRules.requiresAdmin("/temperature/create", "POST"));
    }

    @Test
    void pressureListRequiresLoginButNotAdmin() {
        assertTrue(AuthPathRules.requiresLogin("/pressure/list"));
        assertFalse(AuthPathRules.requiresAdmin("/pressure/list", "GET"));
        assertTrue(AuthPathRules.requiresAdmin("/pressure/create", "POST"));
    }

    @Test
    void sleepListRequiresLoginButNotAdmin() {
        assertTrue(AuthPathRules.requiresLogin("/sleep/list"));
        assertFalse(AuthPathRules.requiresAdmin("/sleep/list", "GET"));
        assertTrue(AuthPathRules.requiresAdmin("/sleep/create", "POST"));
    }

    @Test
    void alertConfigListRequiresLoginButNotAdmin() {
        assertTrue(AuthPathRules.requiresLogin("/alert-config/list"));
        assertFalse(AuthPathRules.requiresAdmin("/alert-config/list", "GET"));
        assertTrue(AuthPathRules.requiresLogin("/alert-config/evaluate"));
        assertFalse(AuthPathRules.requiresAdmin("/alert-config/evaluate", "POST"));
    }

    @Test
    void alertConfigWritesRequireAdmin() {
        assertTrue(AuthPathRules.requiresAdmin("/alert-config/update", "PUT"));
        assertTrue(AuthPathRules.requiresAdmin("/alert-config/toggle/3", "PUT"));
    }

    @Test
    void helloDoesNotRequireLogin() {
        assertFalse(AuthPathRules.requiresLogin("/hello"));
        assertFalse(AuthPathRules.requiresAdmin("/hello", "GET"));
    }
}
