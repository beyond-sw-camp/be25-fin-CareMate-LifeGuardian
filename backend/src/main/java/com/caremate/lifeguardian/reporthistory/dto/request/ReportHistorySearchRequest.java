package com.caremate.lifeguardian.reporthistory.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportHistorySearchRequest {

    private String sendType;

    private String sendItemType;

    private String sendStatus;

    private String keyword;

    private int page;

    private int size;

    public int getOffset() {
        return (page - 1) * size;
    }
}
