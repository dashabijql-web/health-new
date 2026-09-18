package com.xzkj.health.controller;

import com.xzkj.health.model.dto.SleepPage;
import com.xzkj.health.model.dto.SleepRecord;
import com.xzkj.health.service.SleepService;
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

@WebMvcTest(SleepController.class)
@Import(RestExceptionHandler.class)
@TestPropertySource(properties = "health.auth.enabled=false")
class SleepControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    SleepService sleepService;

    @Test
    void returnsSleepPage() throws Exception {
        SleepRecord record = new SleepRecord();
        record.setSleepMinutes(480);
        given(sleepService.list("EMP0001", null, null, 1, 20))
                .willReturn(new SleepPage(List.of(record), 1, 1, 20));

        mockMvc.perform(get("/sleep/list")
                        .param("empCode", "EMP0001")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.list[0].sleepMinutes").value(480));
    }

    @Test
    void returnsEmptySleepPage() throws Exception {
        given(sleepService.list("UNKNOWN", null, null, 1, 20))
                .willReturn(new SleepPage(List.of(), 0, 1, 20));

        mockMvc.perform(get("/sleep/list")
                        .param("empCode", "UNKNOWN")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.list").isEmpty());
    }

    @Test
    void rejectsCreateWhenServiceValidationFails() throws Exception {
        given(sleepService.create("EMP0001", 0, null))
                .willThrow(new IllegalArgumentException("睡眠时长必须在1到1439分钟之间"));

        mockMvc.perform(post("/sleep/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empCode\":\"EMP0001\",\"sleepMinutes\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("睡眠时长必须在1到1439分钟之间"));
    }
}
