package com.caremate.lifeguardian.member.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesUser {

    private Long id;
    private Long branchId;
    private String branchName;
    private String employeeId;
    private String passwordHash;
    private String name;
    private LocalDate birthDate;
    private String email;
    private String phone;
    private String rankCode;
    private String roleCode;
    private String statusCode;
    private Boolean isTempPassword;
    private Boolean termsAgreed;
    private LocalDate joinedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 사번을 할당(동기화)하기 위한 메서드
    public void assignEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    // 영업사원의 계정 상태를 변경하는 메서드
    public void changeStatus(String statusCode) {
        this.statusCode = statusCode;
    }

    // 영업사원 기본 정보를 수정하는 메서드
    public void updateInfo(String name, LocalDate birthDate, Long branchId, String rankCode, String phone, String email, LocalDate joinedAt) {
        this.name = name;
        this.birthDate = birthDate;
        this.branchId = branchId;
        this.rankCode = rankCode;
        this.phone = phone;
        this.email = email;
        this.joinedAt = joinedAt;
    }
}
