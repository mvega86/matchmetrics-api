package com.matchmetrics.mapper.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoadmapItemDTO {
    private String id;
    private String title;
    private String description;
    private String horizon;      // SHORT | MEDIUM | LONG | VERY_LONG
    private String priority;     // HIGH | MEDIUM | LOW
    private String complexity;   // LOW | MEDIUM | HIGH
    private boolean breakingChange;
    private String status;       // PENDING | IN_PROGRESS | COMPLETED
    private String benefit;
}
