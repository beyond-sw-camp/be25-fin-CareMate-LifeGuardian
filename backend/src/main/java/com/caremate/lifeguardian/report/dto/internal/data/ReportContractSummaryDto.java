package com.caremate.lifeguardian.report.dto.internal.data;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

@Getter
@Setter
@Alias("ReportContractSummaryDto")
public class ReportContractSummaryDto {

    private Long contractId;
    private String policyNumber;
    private String contractorName;
    private String insuredName;
    private String productName;
    private String categoryName;
    private String contractStatusName;
    private String contractResultStatusName;
    private LocalDateTime contractDate;
}
