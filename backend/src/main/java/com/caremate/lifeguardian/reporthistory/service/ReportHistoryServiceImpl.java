package com.caremate.lifeguardian.reporthistory.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.reporthistory.dto.request.ReportHistorySearchRequest;
import com.caremate.lifeguardian.reporthistory.dto.response.ReportHistoryItemResponse;
import com.caremate.lifeguardian.reporthistory.dto.response.ReportHistoryPageResponse;
import com.caremate.lifeguardian.reporthistory.mapper.ReportHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReportHistoryServiceImpl implements ReportHistoryService {

    private static final Set<String> SEND_TYPES = Set.of("all", "report", "webform");
    private static final Set<String> SEND_ITEM_TYPES = Set.of("all", "report_lifecycle", "report_disease", "webform");
    private static final Set<String> SEND_STATUSES = Set.of("all", "pending", "success", "failed", "collected");
    private static final Set<String> CUSTOMER_STAGE_CODES = Set.of("all", "01", "02");

    private final ReportHistoryMapper reportHistoryMapper;

    @Override
    @Transactional(readOnly = true)
    public ReportHistoryPageResponse getReportHistory(Long currentUserId, ReportHistorySearchRequest request) {
        ReportHistorySearchRequest normalizedRequest = validateAndNormalizeRequest(currentUserId, request);

        try {
            long totalCount = reportHistoryMapper.countReportHistory(currentUserId, normalizedRequest);
            int totalPages = (int) Math.ceil((double) totalCount / normalizedRequest.getSize());

            List<ReportHistoryItemResponse> items = totalCount == 0
                    ? Collections.emptyList()
                    : reportHistoryMapper.findReportHistory(currentUserId, normalizedRequest);

            return ReportHistoryPageResponse.builder()
                    .page(normalizedRequest.getPage())
                    .size(normalizedRequest.getSize())
                    .totalCount(totalCount)
                    .totalPages(totalPages)
                    .items(items)
                    .build();
        } catch (DataAccessException e) {
            throw new BaseException(500, "발송 내역을 조회하지 못했습니다. 관리자에게 문의하세요.");
        }
    }

    private ReportHistorySearchRequest validateAndNormalizeRequest(Long currentUserId, ReportHistorySearchRequest request) {
        if (currentUserId == null || currentUserId < 1) {
            throw new BaseException(400, "영업사원 ID는 1 이상이어야 합니다.");
        }

        if (request == null) {
            throw new BaseException(400, "검색 조건은 필수입니다.");
        }

        String sendType = normalizeOrDefault(request.getSendType(), "all");

        if (!SEND_TYPES.contains(sendType)) {
            throw new BaseException(400, "발송 유형은 all, report, webform 중 하나여야 합니다.");
        }

        String sendItemType = normalizeOrDefault(request.getSendItemType(), "all");

        if (!SEND_ITEM_TYPES.contains(sendItemType)) {
            throw new BaseException(400, "발송 항목은 all, report_lifecycle, report_disease, webform 중 하나여야 합니다.");
        }

        String sendStatus = normalizeOrDefault(request.getSendStatus(), "all");

        if (!SEND_STATUSES.contains(sendStatus)) {
            throw new BaseException(400, "발송 상태는 all, pending, success, failed, collected 중 하나여야 합니다.");
        }

        String customerStageCode = normalizeOrDefault(request.getCustomerStageCode(), "all");

        if (!CUSTOMER_STAGE_CODES.contains(customerStageCode)) {
            throw new BaseException(400, "고객 유형은 all, 01, 02 중 하나여야 합니다.");
        }

        String keyword = request.getKeyword() == null ? null : request.getKeyword().trim();
        if (keyword != null && keyword.matches(".*\\s+.*")) {
            throw new BaseException(400, "고객명 검색어 중간에는 공백을 입력할 수 없습니다.");
        }

        if (request.getPage() < 1) {
            throw new BaseException(400, "페이지 번호는 1 이상이어야 합니다.");
        }

        if (request.getSize() < 1) {
            throw new BaseException(400, "페이지 크기는 1 이상이어야 합니다.");
        }

        if (request.getSize() > 100) {
            throw new BaseException(400, "페이지 크기는 100 이하여야 합니다.");
        }

        return ReportHistorySearchRequest.builder()
                .sendType(sendType)
                .sendItemType(sendItemType)
                .sendStatus(sendStatus)
                .customerStageCode(customerStageCode)
                .keyword(keyword)
                .page(request.getPage())
                .size(request.getSize())
                .build();
    }

    private String normalizeOrDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value.trim().toLowerCase();
    }
}
