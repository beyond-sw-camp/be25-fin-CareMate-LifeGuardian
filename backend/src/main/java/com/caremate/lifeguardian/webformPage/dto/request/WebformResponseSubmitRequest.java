package com.caremate.lifeguardian.webformPage.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebformResponseSubmitRequest {
    private Long customerId;
    private String conversionStatusCode;
    private String uuidToken;
    private BigDecimal height;
    private BigDecimal weight;
    private String selectedPriorityCategory;
    private List<String> historyList;
    private List<String> activityList;
    private Boolean pastSurgeryOrHospitalization;
    private String desiredBudgetCode;
}
