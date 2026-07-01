package com.caremate.lifeguardian.webformPage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebformIssuanceDto {
    private Long customerId;
    private String conversionStatusCode;
    private String webformStatusCode;
}
