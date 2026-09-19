package com.xzkj.health.service;

import com.xzkj.health.model.dto.WarningCodeCatalogItem;
import com.xzkj.health.model.dto.WarningSourceCatalog;
import com.xzkj.health.model.entity.WarningRecord;
import com.xzkj.health.model.enums.WarningEventCode;
import com.xzkj.health.model.enums.WarningEventSource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class WarningClassificationService {
    public List<WarningSourceCatalog> catalog() {
        return Arrays.stream(WarningEventSource.values())
                .map(source -> new WarningSourceCatalog(
                        source.name(),
                        source.getLabel(),
                        Arrays.stream(WarningEventCode.values())
                                .filter(code -> code.getSource() == source)
                                .map(code -> new WarningCodeCatalogItem(code.name(), code.getLabel()))
                                .toList()))
                .toList();
    }

    public void validate(WarningRecord warning) {
        if (warning == null) {
            throw new IllegalArgumentException("预警记录不能为空");
        }

        WarningEventSource source = parseSource(warning.getEventSource());
        WarningEventCode code = parseCode(warning.getEventCode());
        if (code.getSource() != source) {
            throw new IllegalArgumentException("事件代码不属于指定的预警来源");
        }
        if (source == WarningEventSource.DEVICE_ALARM
                && (warning.getDeviceImei() == null || warning.getDeviceImei().isBlank())) {
            throw new IllegalArgumentException("设备报警必须包含设备IMEI");
        }
        if (source == WarningEventSource.HEALTH_THRESHOLD
                && (warning.getThresholdSnapshot() == null || warning.getThresholdSnapshot().isBlank())) {
            throw new IllegalArgumentException("健康阈值预警必须包含阈值快照");
        }
    }

    private WarningEventSource parseSource(String value) {
        try {
            return WarningEventSource.valueOf(value == null ? "" : value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("未知的预警来源");
        }
    }

    private WarningEventCode parseCode(String value) {
        try {
            return WarningEventCode.valueOf(value == null ? "" : value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("未知的事件代码");
        }
    }
}
