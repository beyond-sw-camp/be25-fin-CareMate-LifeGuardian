package com.caremate.lifeguardian.webformPage.service;

import com.caremate.lifeguardian.recommendation.domain.WebformResponse;
import com.caremate.lifeguardian.webform.mapper.WebformMapper;
import com.caremate.lifeguardian.webformPage.mapper.WebformPageMapper;
import com.caremate.lifeguardian.webformPage.dto.request.WebformResponseSubmitRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WebformPageServiceImpl implements WebformPageService {

    private final WebformMapper webformMapper;
    private final WebformPageMapper webformPageMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void submitWebformResponse(WebformResponseSubmitRequest request) {
        // 고객 ID 및 고객 유형 존재 여부 검증
        if ("01".equals(request.getConversionStatusCode())) {
            if (!webformPageMapper.existsPotentialCustomerById(request.getCustomerId())) {
                throw new IllegalArgumentException("존재하지 않는 잠재 고객 ID입니다.");
            }
        } else if ("02".equals(request.getConversionStatusCode())) {
            if (!webformPageMapper.existsIntegratedCustomerById(request.getCustomerId())) {
                throw new IllegalArgumentException("존재하지 않는 통합 고객 ID입니다.");
            }
        } else {
            throw new IllegalArgumentException("올바르지 않은 고객 유형 구분 코드입니다.");
        }

        String historyJson = "[]";
        String activityJson = "[]";
        try {
            if (request.getHistoryList() != null) {
                historyJson = objectMapper.writeValueAsString(request.getHistoryList());
            }
            if (request.getActivityList() != null) {
                activityJson = objectMapper.writeValueAsString(request.getActivityList());
            }
        } catch (Exception e) {
            throw new RuntimeException("JSON 변환 중 오류가 발생했습니다.", e);
        }

        WebformResponse response = WebformResponse.builder()
                .customerId(request.getCustomerId())
                .conversionStatusCode(request.getConversionStatusCode())
                .height(request.getHeight())
                .weight(request.getWeight())
                .selectedPriorityCategory(request.getSelectedPriorityCategory())
                .historyJson(historyJson)
                .activityJson(activityJson)
                .pastSurgeryOrHospitalization(request.getPastSurgeryOrHospitalization())
                .desiredBudgetCode(request.getDesiredBudgetCode())
                .build();

        webformPageMapper.insertWebformResponse(response);

        // 만약 uuidToken이 존재한다면, 기존 webform 회수 로직을 적용하여 발송 상태를 '04'(회수)로 변경
        if (request.getUuidToken() != null && !request.getUuidToken().trim().isEmpty()) {
            webformMapper.updateWebformCollected(request.getUuidToken());
        }

        // 고객 유형이 잠재 고객('01')인 경우, 영업 상태를 "상담중('02')"으로 변경하고 수신 일시를 오늘 날짜로 갱신
        if ("01".equals(request.getConversionStatusCode())) {
            webformMapper.updatePotentialCustomerConsultStatus(request.getCustomerId());
        }
    }

    @Override
    public String getCustomerName(Long customerId, String conversionStatusCode) {
        if ("01".equals(conversionStatusCode)) {
            return webformPageMapper.findPotentialCustomerNameById(customerId);
        } else if ("02".equals(conversionStatusCode)) {
            return webformPageMapper.findIntegratedCustomerNameById(customerId);
        }
        return null;
    }
}
