package com.caremate.lifeguardian.report.service;

import com.caremate.lifeguardian.report.dto.internal.ReportTargetDto;
import com.caremate.lifeguardian.report.dto.response.ReportBulkSendResultDto;
import com.caremate.lifeguardian.report.dto.response.ReportCreateResultDto;
import com.caremate.lifeguardian.report.dto.response.ReportPreviewUrlResponse;
import com.caremate.lifeguardian.report.dto.response.ReportSendResultDto;

import java.util.List;

public interface ReportService {

    List<ReportCreateResultDto> createReports(List<ReportTargetDto> targets);

    ReportPreviewUrlResponse createReportPreviewUrl(Long reportId, Long currentUserId);

    ReportSendResultDto sendReport(
            Long customerId,
            String conversionStatusCode,
            Long currentUserId,
            String ipAddress,
            String userAgent
    );

    ReportBulkSendResultDto sendReportsInBulk(
            Long currentUserId,
            String ipAddress,
            String userAgent,
            List<Long> reportIds
    );
}
