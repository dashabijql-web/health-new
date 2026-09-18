package com.xzkj.health.controller;

import com.xzkj.health.model.dto.BloodPressurePage;
import com.xzkj.health.model.dto.BloodPressureRecord;
import com.xzkj.health.service.BloodPressureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BloodPressureController.class)
@Import(RestExceptionHandler.class)
@TestPropertySource(properties = "health.auth.enabled=false")
class BloodPressureControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    BloodPressureService bloodPressureService;

    @Test
    void returnsBloodPressurePage() throws Exception {
        BloodPressureRecord record = new BloodPressureRecord();
        record.setSystolic(120);
        record.setDiastolic(80);
        record.setEmpCode("EMP0001");
        given(bloodPressureService.list("EMP0001", null, null, 1, 20))
                .willReturn(new BloodPressurePage(List.of(record), 1, 1, 20));

        mockMvc.perform(get("/blood-pressure/list")
                        .param("empCode", "EMP0001")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.list[0].systolic").value(120))
                .andExpect(jsonPath("$.list[0].diastolic").value(80));
    }

    @Test
    void returnsBloodPressureTrend() throws Exception {
        BloodPressureRecord record = new BloodPressureRecord();
        record.setSystolic(128);
        record.setDiastolic(82);
        given(bloodPressureService.trend("EMP0001", "2026-09-01", "2026-09-18"))
                .willReturn(List.of(record));

        mockMvc.perform(get("/blood-pressure/trend")
                        .param("empCode", "EMP0001")
                        .param("startTime", "2026-09-01")
                        .param("endTime", "2026-09-18"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].systolic").value(128))
                .andExpect(jsonPath("$[0].diastolic").value(82));
    }

    @Test
    void rejectsCreateWhenServiceValidationFails() throws Exception {
        given(bloodPressureService.create("EMP0001", 80, 120, null))
                .willThrow(new IllegalArgumentException("收缩压必须高于舒张压"));

        mockMvc.perform(post("/blood-pressure/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empCode\":\"EMP0001\",\"systolic\":80,\"diastolic\":120}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("收缩压必须高于舒张压"));
    }
}
