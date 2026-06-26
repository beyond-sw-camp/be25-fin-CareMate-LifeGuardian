package com.caremate.lifeguardian.admin.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesUserPerformanceDetail {
    private int rank;
    private String employeeId;
    private String employeeName;
    private String positionName;
    private int thisMonthCount;
    private int annualCount;
    private int monthlyTargetCount;
    private int targetDifference;
}
