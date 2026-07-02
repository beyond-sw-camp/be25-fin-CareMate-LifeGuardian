package com.caremate.lifeguardian.member.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesUserInfo {
    // 목록 내 개별 영업사원 정보를 담는 DTO
    private Long id;
    private String employeeId;
    private String name;
    private String statusCode;
    private String statusName;
    private Integer customerCount;
    private java.time.LocalDate birthDate;
    private Long branchId;
    private String branchName;
    private String rankCode;
    private String positionName;
    private String phone;
    private String email;
    private java.time.LocalDate joinedAt;
    private Integer monthlyTarget;
}
