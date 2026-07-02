package com.caremate.lifeguardian.reporthistory.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReportHistorySearchRequest {

    private String sendType;

    private String sendItemType;

    private String sendStatus;

    private String customerStageCode;

    private String keyword;

    private List<String> keywordTerms;

    private String customerStageCode;

    private int page;

    private int size;

    public int getOffset() {
        return (page - 1) * size;
    }
}
