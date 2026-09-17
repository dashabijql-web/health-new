package com.xzkj.health.controller;

import com.xzkj.health.model.dto.DeviceListItem;
import com.xzkj.health.model.dto.DevicePage;
import com.xzkj.health.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
@TestPropertySource(properties = "health.auth.enabled=false")
class DeviceControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    DeviceService deviceService;

    @Test
    void returnsDevicePage() throws Exception {
        DeviceListItem item = new DeviceListItem();
        item.setId(152L);
        item.setImei("359456780000001");
        item.setEmpName("张伟");
        given(deviceService.list("3594", 1, 20))
                .willReturn(new DevicePage(List.of(item), 1, 1, 20));

        mockMvc.perform(get("/device/list")
                        .param("keyword", "3594")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.list[0].imei").value("359456780000001"))
                .andExpect(jsonPath("$.list[0].empName").value("张伟"));
    }
}
