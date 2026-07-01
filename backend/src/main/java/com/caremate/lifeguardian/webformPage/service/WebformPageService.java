package com.caremate.lifeguardian.webformPage.service;

import com.caremate.lifeguardian.webformPage.dto.request.WebformResponseSubmitRequest;
import com.caremate.lifeguardian.webformPage.dto.response.WebformTokenVerifyResponse;

public interface WebformPageService {
    void submitWebformResponse(WebformResponseSubmitRequest request);
    String getCustomerName(Long customerId, String conversionStatusCode);
    WebformTokenVerifyResponse verifyTokenAndGetCustomerInfo(String token);
}
