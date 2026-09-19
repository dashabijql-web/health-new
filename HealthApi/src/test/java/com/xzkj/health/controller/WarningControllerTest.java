package com.xzkj.health.controller;

import com.xzkj.health.model.dto.HealthThresholdEvaluation;
import com.xzkj.health.model.dto.WarningGenerationResult;
import com.xzkj.health.model.entity.WarningRecord;
import com.xzkj.health.service.HealthWarningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WarningController.class)
@Import(RestExceptionHandler.class)
@TestPropertySource(properties = "health.auth.enabled=false")
class WarningControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    HealthWarningService healthWarningService;

    @Test
    void generatesStructuredWarning() throws Exception {
        HealthThresholdEvaluation evaluation = new HealthThresholdEvaluation(
                "EMP0001", 1, 21L, "心率", "bpm", new BigDecimal("161"),
                "HIGH", "HIGH", true, 3, false);
        WarningRecord warning = new WarningRecord();
        warning.setId(88L);
        warning.setEventSource("HEALTH_THRESHOLD");
        warning.setEventCode("HEART_RATE");
        warning.setWarningLevel("高危");
        given(healthWarningService.evaluateAndCreate("EMP0001", 1, new BigDecimal("161")))
                .willReturn(new WarningGenerationResult(evaluation, true, warning));

        mockMvc.perform(post("/warning/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empCode\":\"EMP0001\",\"configType\":1,\"value\":161}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created").value(true))
                .andExpect(jsonPath("$.warning.id").value(88))
                .andExpect(jsonPath("$.warning.eventSource").value("HEALTH_THRESHOLD"))
                .andExpect(jsonPath("$.warning.eventCode").value("HEART_RATE"))
                .andExpect(jsonPath("$.warning.warningLevel").value("高危"));
    }
}
