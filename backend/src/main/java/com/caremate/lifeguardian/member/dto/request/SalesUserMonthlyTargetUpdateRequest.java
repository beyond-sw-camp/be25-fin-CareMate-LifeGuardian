package com.caremate.lifeguardian.member.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalesUserMonthlyTargetUpdateRequest {

    @NotBlank(message = "목표 연월은 필수 입력 항목입니다.")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "올바른 연월 형식(YYYY-MM)이 아닙니다.")
    private String targetYearMonth; // "YYYY-MM"

    @NotNull(message = "목표 계약 건수는 필수 입력 항목입니다.")
    @Min(value = 0, message = "목표 계약 건수는 0 이상이어야 합니다.")
    private Integer targetContractCount;
}
