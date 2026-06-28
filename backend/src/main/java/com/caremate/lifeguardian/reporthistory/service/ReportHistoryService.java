package com.caremate.lifeguardian.reporthistory.service;

import com.caremate.lifeguardian.reporthistory.dto.request.ReportHistorySearchRequest;
import com.caremate.lifeguardian.reporthistory.dto.response.ReportHistoryPageResponse;

public interface ReportHistoryService {

    ReportHistoryPageResponse getReportHistory(Long currentUserId, ReportHistorySearchRequest request);
}
