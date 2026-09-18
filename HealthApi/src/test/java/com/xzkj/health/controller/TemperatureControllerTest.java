package com.xzkj.health.controller;

import com.xzkj.health.model.dto.TemperaturePage;
import com.xzkj.health.model.dto.TemperatureRecord;
import com.xzkj.health.service.TemperatureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TemperatureController.class)
@Import(RestExceptionHandler.class)
@TestPropertySource(properties = "health.auth.enabled=false")
class TemperatureControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    TemperatureService temperatureService;

    @Test
    void returnsTemperaturePage() throws Exception {
        TemperatureRecord record = new TemperatureRecord();
        record.setTemperature(new BigDecimal("36.7"));
        given(temperatureService.list("EMP0001", null, null, 1, 20))
                .willReturn(new TemperaturePage(List.of(record), 1, 1, 20));

        mockMvc.perform(get("/temperature/list")
                        .param("empCode", "EMP0001")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.list[0].temperature").value(36.7));
    }

    @Test
    void returnsEmptyTemperaturePage() throws Exception {
        given(temperatureService.list("UNKNOWN", null, null, 1, 20))
                .willReturn(new TemperaturePage(List.of(), 0, 1, 20));

        mockMvc.perform(get("/temperature/list")
                        .param("empCode", "UNKNOWN")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.list").isEmpty());
    }

    @Test
    void rejectsCreateWhenServiceValidationFails() throws Exception {
        given(temperatureService.create(eq("EMP0001"), any(BigDecimal.class), eq(null)))
                .willThrow(new IllegalArgumentException("体温必须在35.0到42.0℃之间"));

        mockMvc.perform(post("/temperature/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empCode\":\"EMP0001\",\"temperature\":34.9}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("体温必须在35.0到42.0℃之间"));
    }
}
