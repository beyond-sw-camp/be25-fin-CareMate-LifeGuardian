package com.caremate.lifeguardian.reporthistory.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

@Getter
@Setter
@Alias("ReportHistoryItem")
public class ReportHistoryItemResponse {

    private Long id;

    private String sendType;

    private String sendTypeName;

    private Long customerId;

    private String customerName;

    private String customerStageCode;

    private String customerStageName;

    private String sendItemCode;

    private String sendItemName;

    private String sendStatusCode;

    private String sendStatusName;

    private LocalDateTime sentAt;
}
