package com.caremate.lifeguardian.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesUserPerformanceDetailResponse {
    private int rank;
    private String groupCode;
    private String employeeId;
    private String employeeName;
    private String positionName;
    private int thisMonthCount;
    private int annualCount;
    private int monthlyTargetCount;
    private int targetDifference;
}
