package com.caremate.lifeguardian.reporthistory.controller;

import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.common.security.SecurityUtil;
import com.caremate.lifeguardian.reporthistory.dto.request.ReportHistorySearchRequest;
import com.caremate.lifeguardian.reporthistory.dto.response.ReportHistoryPageResponse;
import com.caremate.lifeguardian.reporthistory.service.ReportHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "발송 내역 API", description = "리포트 및 웹폼 발송 내역 조회 API입니다.")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/report-history")
public class ReportHistoryController {

    private final ReportHistoryService reportHistoryService;

    @Operation(summary = "발송 내역 조회", description = "로그인한 설계사가 담당하는 고객의 리포트 및 웹폼 발송 내역을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<ReportHistoryPageResponse>> getReportHistory(
            @RequestParam(defaultValue = "all") String sendType,
            @RequestParam(defaultValue = "all") String sendItemType,
            @RequestParam(defaultValue = "all") String sendStatus,
            @RequestParam(defaultValue = "all") String customerStageCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        ReportHistorySearchRequest request = ReportHistorySearchRequest.builder()
                .sendType(sendType)
                .sendItemType(sendItemType)
                .sendStatus(sendStatus)
                .customerStageCode(customerStageCode)
                .keyword(keyword)
                .page(page)
                .size(size)
                .build();

        ReportHistoryPageResponse response = reportHistoryService.getReportHistory(currentUserId, request);

        return ResponseEntity.ok(
                ApiResponse.success(200, "발송 내역 조회에 성공했습니다.", response)
        );
    }
}
