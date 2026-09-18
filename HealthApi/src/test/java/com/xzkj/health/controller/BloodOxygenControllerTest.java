package com.xzkj.health.controller;

import com.xzkj.health.model.dto.BloodOxygenPage;
import com.xzkj.health.model.dto.BloodOxygenRecord;
import com.xzkj.health.service.BloodOxygenService;
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

@WebMvcTest(BloodOxygenController.class)
@Import(RestExceptionHandler.class)
@TestPropertySource(properties = "health.auth.enabled=false")
class BloodOxygenControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    BloodOxygenService bloodOxygenService;

    @Test
    void returnsBloodOxygenPage() throws Exception {
        BloodOxygenRecord record = new BloodOxygenRecord();
        record.setBloodOxygen(98);
        record.setEmpCode("EMP0001");
        given(bloodOxygenService.list("EMP0001", null, null, 1, 20))
                .willReturn(new BloodOxygenPage(List.of(record), 1, 1, 20));

        mockMvc.perform(get("/blood-oxygen/list")
                        .param("empCode", "EMP0001")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.list[0].bloodOxygen").value(98));
    }

    @Test
    void returnsBloodOxygenTrend() throws Exception {
        BloodOxygenRecord record = new BloodOxygenRecord();
        record.setBloodOxygen(97);
        given(bloodOxygenService.trend("EMP0001", "2026-09-01", "2026-09-18"))
                .willReturn(List.of(record));

        mockMvc.perform(get("/blood-oxygen/trend")
                        .param("empCode", "EMP0001")
                        .param("startTime", "2026-09-01")
                        .param("endTime", "2026-09-18"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bloodOxygen").value(97));
    }

    @Test
    void rejectsCreateWhenServiceValidationFails() throws Exception {
        given(bloodOxygenService.create("EMP0001", 49, null))
                .willThrow(new IllegalArgumentException("血氧必须在50到100之间"));

        mockMvc.perform(post("/blood-oxygen/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empCode\":\"EMP0001\",\"bloodOxygen\":49}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("血氧必须在50到100之间"));
    }
}
