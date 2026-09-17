package com.xzkj.health.service;

import com.xzkj.health.mapper.DeviceMapper;
import com.xzkj.health.model.entity.Device;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {
    @Mock
    DeviceMapper deviceMapper;

    @InjectMocks
    DeviceService deviceService;

    @Test
    void createsDeviceWhenImeiIsValid() {
        Device device = new Device();
        device.setImei(" 999000000000001 ");
        when(deviceMapper.countByImei("999000000000001", null)).thenReturn(0L);
        when(deviceMapper.insert(any(Device.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, Device.class).setId(3000L);
            return 1;
        });

        Device created = deviceService.create(device);

        assertEquals(3000L, created.getId());
        assertEquals("999000000000001", created.getImei());
        assertEquals("watch", created.getDeviceType());
        assertEquals(0, created.getStatus());
        assertEquals(0, created.getOnlineStatus());
        verify(deviceMapper).insert(device);
    }

    @Test
    void rejectsInvalidImei() {
        Device device = new Device();
        device.setImei("abc");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deviceService.create(device));

        assertEquals("IMEI必须是15位数字", exception.getMessage());
        verify(deviceMapper, never()).insert(any(Device.class));
    }

    @Test
    void rejectsDuplicateImei() {
        Device device = new Device();
        device.setImei("359456780000001");
        when(deviceMapper.countByImei("359456780000001", null)).thenReturn(1L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deviceService.create(device));

        assertEquals("IMEI已存在", exception.getMessage());
        verify(deviceMapper, never()).insert(any(Device.class));
    }

    @Test
    void rejectsDeleteWhenDeviceIsBound() {
        Device existing = new Device();
        existing.setId(152L);
        when(deviceMapper.selectById(152L)).thenReturn(existing);
        when(deviceMapper.countCurrentBindings(152L)).thenReturn(1L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deviceService.delete(152L));

        assertEquals("设备已绑定职工，请先解绑", exception.getMessage());
        verify(deviceMapper, never()).deleteById(152L);
    }
}
