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

    private final ReportHistoryMapper reportHistoryMapper;

    @Override
    @Transactional(readOnly = true)
    public ReportHistoryPageResponse getReportHistory(Long currentUserId, ReportHistorySearchRequest request) {
        validateRequest(currentUserId, request);

        try {
            long totalCount = reportHistoryMapper.countReportHistory(currentUserId, request);
            int totalPages = (int) Math.ceil((double) totalCount / request.getSize());

            List<ReportHistoryItemResponse> items = totalCount == 0
                    ? Collections.emptyList()
                    : reportHistoryMapper.findReportHistory(currentUserId, request);

            return ReportHistoryPageResponse.builder()
                    .page(request.getPage())
                    .size(request.getSize())
                    .totalCount(totalCount)
                    .totalPages(totalPages)
                    .items(items)
                    .build();
        } catch (DataAccessException e) {
            throw new BaseException(500, "발송 내역을 조회하지 못했습니다. 관리자에게 문의하세요.");
        }
    }

    private void validateRequest(Long currentUserId, ReportHistorySearchRequest request) {
        if (currentUserId == null || currentUserId < 1) {
            throw new BaseException(400, "영업사원 ID는 1 이상이어야 합니다.");
        }

        if (request == null) {
            throw new BaseException(400, "검색 조건은 필수입니다.");
        }

        if (request.getSendType() == null || request.getSendType().isBlank()) {
            request.setSendType("all");
        }

        request.setSendType(request.getSendType().trim().toLowerCase());

        if (!SEND_TYPES.contains(request.getSendType())) {
            throw new BaseException(400, "발송 유형은 all, report, webform 중 하나여야 합니다.");
        }

        if (request.getSendItemType() == null || request.getSendItemType().isBlank()) {
            request.setSendItemType("all");
        }

        request.setSendItemType(request.getSendItemType().trim().toLowerCase());

        if (!SEND_ITEM_TYPES.contains(request.getSendItemType())) {
            throw new BaseException(400, "발송 항목은 all, report_lifecycle, report_disease, webform 중 하나여야 합니다.");
        }

        if (request.getSendStatus() == null || request.getSendStatus().isBlank()) {
            request.setSendStatus("all");
        }

        request.setSendStatus(request.getSendStatus().trim().toLowerCase());

        if (!SEND_STATUSES.contains(request.getSendStatus())) {
            throw new BaseException(400, "발송 상태는 all, pending, success, failed, collected 중 하나여야 합니다.");
        }

        if (request.getKeyword() != null) {
            request.setKeyword(request.getKeyword().trim());
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
    }
}
