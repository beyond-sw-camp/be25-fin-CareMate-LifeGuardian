package com.caremate.lifeguardian.reporthistory.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportHistorySearchRequest {

    private String sendType = "all";

    private String sendItemType = "all";

    private String sendStatus = "all";

    private String keyword;

    private int page = 1;

    private int size = 10;

    public int getOffset() {
        return (page - 1) * size;
    }
}
