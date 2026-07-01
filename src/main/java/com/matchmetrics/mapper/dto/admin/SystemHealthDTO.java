package com.matchmetrics.mapper.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SystemHealthDTO {
    private SystemMetricsDTO metrics;
    private List<SystemAlertDTO> alerts;
    private LocalDateTime generatedAt;
}
