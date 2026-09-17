package com.xzkj.health.controller;

import com.xzkj.health.model.dto.DeviceListItem;
import com.xzkj.health.model.dto.DevicePage;
import com.xzkj.health.model.entity.Device;
import com.xzkj.health.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
@Import(RestExceptionHandler.class)
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

    @Test
    void createsDevice() throws Exception {
        given(deviceService.create(any(Device.class))).willAnswer(invocation -> {
            Device device = invocation.getArgument(0);
            device.setId(3000L);
            device.setImei("999000000000001");
            return device;
        });

        mockMvc.perform(post("/device/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imei\":\"999000000000001\",\"deviceType\":\"watch\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3000))
                .andExpect(jsonPath("$.imei").value("999000000000001"));
    }

    @Test
    void rejectsCreateWhenServiceValidationFails() throws Exception {
        given(deviceService.create(any(Device.class)))
                .willThrow(new IllegalArgumentException("IMEI必须是15位数字"));

        mockMvc.perform(post("/device/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imei\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("IMEI必须是15位数字"));
    }

    @Test
    void rejectsDeleteWhenServiceValidationFails() throws Exception {
        doThrow(new IllegalArgumentException("设备已绑定职工，请先解绑")).when(deviceService).delete(152L);

        mockMvc.perform(delete("/device/delete/152"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("设备已绑定职工，请先解绑"));
    }

    @Test
    void bindsDevice() throws Exception {
        mockMvc.perform(post("/device/3000/bind")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empCode\":\"LEARN-EMP\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("绑定成功"));
    }

    @Test
    void unbindsDevice() throws Exception {
        mockMvc.perform(post("/device/3000/unbind"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("解绑成功"));
    }
}
