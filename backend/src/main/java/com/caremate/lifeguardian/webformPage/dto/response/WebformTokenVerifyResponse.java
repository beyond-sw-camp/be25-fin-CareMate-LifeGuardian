package com.caremate.lifeguardian.webformPage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebformTokenVerifyResponse {
    private Long customerId;
    private String conversionStatusCode;
    private String customerName;
}
