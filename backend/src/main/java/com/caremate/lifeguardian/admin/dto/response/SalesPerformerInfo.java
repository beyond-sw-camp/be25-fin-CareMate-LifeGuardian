package com.caremate.lifeguardian.admin.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE) // ◀ Jackson용 기본 생성자 강제 생성
public class SalesPerformerInfo {
    int rank;
    String employeeId;
    String employeeName;
    int contractCount;
}
