package com.caremate.lifeguardian.webformPage.service;

import com.caremate.lifeguardian.webformPage.dto.request.WebformResponseSubmitRequest;

public interface WebformPageService {
    void submitWebformResponse(WebformResponseSubmitRequest request);
    String getCustomerName(Long customerId, String conversionStatusCode);
}
