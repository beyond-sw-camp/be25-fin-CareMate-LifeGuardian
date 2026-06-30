package com.caremate.lifeguardian.report.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class ReportPreviewUrlResponse {
    private Long reportId;
    private String previewUrl;
    private OffsetDateTime expiresAt;
}
