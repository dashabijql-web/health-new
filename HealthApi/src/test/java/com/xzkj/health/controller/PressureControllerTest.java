package com.xzkj.health.controller;

import com.xzkj.health.model.dto.PressurePage;
import com.xzkj.health.model.dto.PressureRecord;
import com.xzkj.health.service.PressureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PressureController.class)
@Import(RestExceptionHandler.class)
@TestPropertySource(properties = "health.auth.enabled=false")
class PressureControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    PressureService pressureService;

    @Test
    void returnsPressurePage() throws Exception {
        PressureRecord record = new PressureRecord();
        record.setPressure(65);
        given(pressureService.list("EMP0001", null, null, 1, 20))
                .willReturn(new PressurePage(List.of(record), 1, 1, 20));

        mockMvc.perform(get("/pressure/list")
                        .param("empCode", "EMP0001")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.list[0].pressure").value(65));
    }

    @Test
    void returnsEmptyPressurePage() throws Exception {
        given(pressureService.list("UNKNOWN", null, null, 1, 20))
                .willReturn(new PressurePage(List.of(), 0, 1, 20));

        mockMvc.perform(get("/pressure/list")
                        .param("empCode", "UNKNOWN")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.list").isEmpty());
    }

    @Test
    void rejectsCreateWhenServiceValidationFails() throws Exception {
        given(pressureService.create("EMP0001", 101, null))
                .willThrow(new IllegalArgumentException("压力指数必须在30到100之间"));

        mockMvc.perform(post("/pressure/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empCode\":\"EMP0001\",\"pressure\":101}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("压力指数必须在30到100之间"));
    }
}
