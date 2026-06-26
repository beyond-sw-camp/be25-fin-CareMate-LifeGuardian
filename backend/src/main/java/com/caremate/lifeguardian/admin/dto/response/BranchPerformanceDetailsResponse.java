package com.caremate.lifeguardian.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchPerformanceDetailsResponse {
    private String targetYearMonth;
    private List<SalesUserPerformanceDetailResponse> performances;
}
