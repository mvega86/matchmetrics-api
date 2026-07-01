package com.matchmetrics.mapper.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemAlertDTO {
    private String id;
    private String level;        // CRITICAL | WARNING | INFO
    private String title;
    private String description;
    private String metric;
    private long currentValue;
    private long threshold;
    private String recommendation;
}
