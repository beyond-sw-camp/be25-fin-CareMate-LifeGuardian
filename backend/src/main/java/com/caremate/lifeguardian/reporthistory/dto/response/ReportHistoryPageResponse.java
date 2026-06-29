package com.caremate.lifeguardian.reporthistory.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReportHistoryPageResponse {

    private int page;

    private int size;

    private long totalCount;

    private int totalPages;

    private List<ReportHistoryItemResponse> items;
}
