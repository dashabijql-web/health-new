package com.xzkj.health.controller;

import com.xzkj.health.model.dto.HeartRatePage;
import com.xzkj.health.model.dto.HeartRateRecord;
import com.xzkj.health.service.HeartRateService;
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

@WebMvcTest(HeartRateController.class)
@Import(RestExceptionHandler.class)
@TestPropertySource(properties = "health.auth.enabled=false")
class HeartRateControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    HeartRateService heartRateService;

    @Test
    void returnsHeartRatePage() throws Exception {
        HeartRateRecord record = new HeartRateRecord();
        record.setHeartRate(78);
        record.setEmpCode("EMP0001");
        given(heartRateService.list("EMP0001", null, null, 1, 20))
                .willReturn(new HeartRatePage(List.of(record), 1, 1, 20));

        mockMvc.perform(get("/heart-rate/list")
                        .param("empCode", "EMP0001")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.list[0].heartRate").value(78));
    }

    @Test
    void rejectsCreateWhenServiceValidationFails() throws Exception {
        given(heartRateService.create("EMP0001", 10, null))
                .willThrow(new IllegalArgumentException("心率必须在20到300之间"));

        mockMvc.perform(post("/heart-rate/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empCode\":\"EMP0001\",\"heartRate\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("心率必须在20到300之间"));
    }
}
