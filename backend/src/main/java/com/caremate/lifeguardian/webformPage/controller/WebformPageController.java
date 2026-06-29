package com.caremate.lifeguardian.webformPage.controller;

import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.webformPage.dto.request.WebformResponseSubmitRequest;
import com.caremate.lifeguardian.webformPage.dto.response.WebformTokenVerifyResponse;
import com.caremate.lifeguardian.webformPage.service.WebformPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "고객 전용 웹폼 API", description = "고객이 제출한 웹폼 문진 데이터를 처리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/webforms")
public class WebformPageController {

    private final WebformPageService webformPageService;

    @Operation(summary = "고객 웹폼 제출", description = "고객이 입력한 신장, 체중 및 문진 결과를 저장하고 영업사원 대시보드를 위한 상태 전이를 실행합니다.")
    @PostMapping("/response")
    public ApiResponse<Void> submitWebformResponse(
            @RequestBody WebformResponseSubmitRequest request
    ) {
        webformPageService.submitWebformResponse(request);
        return ApiResponse.success(
                200,
                "웹폼 답변이 성공적으로 제출되었습니다.",
                null
        );
    }

    @Operation(summary = "고객 ID 및 이름 검증", description = "고객 ID와 유형을 기준으로 등록된 고객의 존재 여부와 이름을 확인합니다.")
    @GetMapping("/customer/verify")
    public ApiResponse<String> verifyCustomer(
            @RequestParam Long customerId,
            @RequestParam String conversionStatusCode
    ) {
        String name = webformPageService.getCustomerName(customerId, conversionStatusCode);
        if (name == null) {
            return ApiResponse.success(404, "존재하지 않는 고객 ID입니다.", null);
        }
        return ApiResponse.success(200, "고객 검증 성공", name);
    }

    @Operation(summary = "발송 토큰 검증 및 고객 정보 자동 매핑", description = "영업사원이 발송한 링크 내 UUID 토큰을 검증하고, 연결된 고객 ID, 유형, 이름을 반환합니다.")
    @GetMapping("/token/verify")
    public ApiResponse<WebformTokenVerifyResponse> verifyToken(
            @RequestParam String token
    ) {
        try {
            WebformTokenVerifyResponse info = webformPageService.verifyTokenAndGetCustomerInfo(token);
            return ApiResponse.success(200, "토큰 검증 성공", info);
        } catch (IllegalArgumentException e) {
            return ApiResponse.success(400, e.getMessage(), null);
        } catch (IllegalStateException e) {
            return ApiResponse.success(410, e.getMessage(), null); // Gone/Expired link status
        }
    }
}
