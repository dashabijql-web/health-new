package com.xzkj.health.controller;

import com.xzkj.health.model.entity.AlertConfig;
import com.xzkj.health.model.dto.EffectiveAlertConfig;
import com.xzkj.health.service.AlertConfigService;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlertConfigController.class)
@Import(RestExceptionHandler.class)
@TestPropertySource(properties = "health.auth.enabled=false")
class AlertConfigControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    AlertConfigService alertConfigService;

    @Test
    void returnsAlertConfigList() throws Exception {
        AlertConfig config = validConfig();
        config.setId(3L);
        given(alertConfigService.list()).willReturn(List.of(config));

        mockMvc.perform(get("/alert-config/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].configName").value("心率"))
                .andExpect(jsonPath("$[0].normalMin").value(60.0));
    }

    @Test
    void returnsEffectiveAlertConfigForEmployeeAndMetric() throws Exception {
        AlertConfig config = validConfig();
        config.setId(3L);
        config.setRiskLevel(2);
        EffectiveAlertConfig effective = new EffectiveAlertConfig("EMP0001", 2, false, config);
        given(alertConfigService.effective("EMP0001", 1)).willReturn(effective);

        mockMvc.perform(get("/alert-config/effective")
                        .param("empCode", "EMP0001")
                        .param("configType", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empCode").value("EMP0001"))
                .andExpect(jsonPath("$.employeeRiskLevel").value(2))
                .andExpect(jsonPath("$.defaultFallback").value(false))
                .andExpect(jsonPath("$.config.id").value(3));
    }

    @Test
    void returnsBadRequestWhenEffectiveConfigCannotBeResolved() throws Exception {
        given(alertConfigService.effective("UNKNOWN", 1))
                .willThrow(new IllegalArgumentException("人员不存在"));

        mockMvc.perform(get("/alert-config/effective")
                        .param("empCode", "UNKNOWN")
                        .param("configType", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("人员不存在"));
    }

    @Test
    void updatesAlertConfig() throws Exception {
        AlertConfig config = validConfig();
        config.setId(3L);
        given(alertConfigService.update(any(AlertConfig.class))).willReturn(config);

        mockMvc.perform(put("/alert-config/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":3,\"normalMin\":60,\"normalMax\":100,"
                                + "\"warnLow\":50,\"warnHigh\":120,\"warnMidLow\":45,"
                                + "\"warnMidHigh\":135,\"criticalLow\":40,\"criticalHigh\":150}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.configName").value("心率"));
    }

    @Test
    void togglesAlertConfig() throws Exception {
        AlertConfig config = validConfig();
        config.setId(3L);
        config.setEnabled(0);
        given(alertConfigService.toggle(3L)).willReturn(config);

        mockMvc.perform(put("/alert-config/toggle/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(0));
    }

    @Test
    void returnsBadRequestWhenUpdateValidationFails() throws Exception {
        doThrow(new IllegalArgumentException("低侧预警阈值不能大于正常下限"))
                .when(alertConfigService).update(any(AlertConfig.class));

        mockMvc.perform(put("/alert-config/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":3,\"normalMin\":60,\"normalMax\":100,"
                                + "\"warnLow\":70,\"warnHigh\":120,\"warnMidLow\":65,"
                                + "\"warnMidHigh\":135,\"criticalLow\":40,\"criticalHigh\":150}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("低侧预警阈值不能大于正常下限"));
    }

    private static AlertConfig validConfig() {
        AlertConfig config = new AlertConfig();
        config.setConfigName("心率");
        config.setConfigType(1);
        config.setUnit("bpm");
        config.setNormalMin(new BigDecimal("60"));
        config.setNormalMax(new BigDecimal("100"));
        config.setWarnLow(new BigDecimal("50"));
        config.setWarnHigh(new BigDecimal("120"));
        config.setWarnMidLow(new BigDecimal("45"));
        config.setWarnMidHigh(new BigDecimal("135"));
        config.setCriticalLow(new BigDecimal("40"));
        config.setCriticalHigh(new BigDecimal("150"));
        config.setEnabled(1);
        return config;
    }
}
