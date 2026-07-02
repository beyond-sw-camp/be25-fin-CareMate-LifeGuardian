package com.caremate.lifeguardian.reporthistory.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.reporthistory.dto.request.ReportHistorySearchRequest;
import com.caremate.lifeguardian.reporthistory.dto.response.ReportHistoryItemResponse;
import com.caremate.lifeguardian.reporthistory.dto.response.ReportHistoryPageResponse;
import com.caremate.lifeguardian.reporthistory.mapper.ReportHistoryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportHistoryServiceImplTest {

    @Mock
    private ReportHistoryMapper reportHistoryMapper;

    @InjectMocks
    private ReportHistoryServiceImpl reportHistoryService;

    @Test
    @DisplayName("발송 내역 조회 시 검색 조건을 정규화하고 페이징 응답을 반환한다")
    void getReportHistoryReturnsPagedItemsWithNormalizedRequest() {
        ReportHistorySearchRequest request = ReportHistorySearchRequest.builder()
                .sendType(" REPORT ")
                .sendItemType(" REPORT_LIFECYCLE ")
                .sendStatus(" SUCCESS ")
                .customerStageCode(" 02 ")
                .keyword("  김민준  ")
                .page(2)
                .size(10)
                .build();
        List<ReportHistoryItemResponse> items = List.of(reportHistoryItem());

        given(reportHistoryMapper.countReportHistory(eq(7L), any(ReportHistorySearchRequest.class)))
                .willReturn(11L);
        given(reportHistoryMapper.findReportHistory(eq(7L), any(ReportHistorySearchRequest.class)))
                .willReturn(items);

        ReportHistoryPageResponse response = reportHistoryService.getReportHistory(7L, request);

        assertThat(response.getPage()).isEqualTo(2);
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalCount()).isEqualTo(11L);
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(response.getItems()).containsExactlyElementsOf(items);

        ArgumentCaptor<ReportHistorySearchRequest> requestCaptor =
                ArgumentCaptor.forClass(ReportHistorySearchRequest.class);
        verify(reportHistoryMapper).countReportHistory(eq(7L), requestCaptor.capture());

        ReportHistorySearchRequest normalizedRequest = requestCaptor.getValue();
        assertThat(normalizedRequest.getSendType()).isEqualTo("report");
        assertThat(normalizedRequest.getSendItemType()).isEqualTo("report_lifecycle");
        assertThat(normalizedRequest.getSendStatus()).isEqualTo("success");
        assertThat(normalizedRequest.getCustomerStageCode()).isEqualTo("02");
        assertThat(normalizedRequest.getKeyword()).isEqualTo("김민준");
        assertThat(normalizedRequest.getOffset()).isEqualTo(10);
    }

    @Test
    @DisplayName("발송 내역이 없으면 목록 조회를 생략하고 빈 목록을 반환한다")
    void getReportHistorySkipsFindWhenTotalCountIsZero() {
        ReportHistorySearchRequest request = validRequest();
        given(reportHistoryMapper.countReportHistory(eq(7L), any(ReportHistorySearchRequest.class)))
                .willReturn(0L);

        ReportHistoryPageResponse response = reportHistoryService.getReportHistory(7L, request);

        assertThat(response.getTotalCount()).isZero();
        assertThat(response.getTotalPages()).isZero();
        assertThat(response.getItems()).isEmpty();
        verify(reportHistoryMapper, never())
                .findReportHistory(eq(7L), any(ReportHistorySearchRequest.class));
    }

    @Test
    @DisplayName("검색 조건이 없으면 400 예외가 발생한다")
    void getReportHistoryThrowsWhenRequestIsNull() {
        assertThatThrownBy(() -> reportHistoryService.getReportHistory(7L, null))
                .isInstanceOfSatisfying(BaseException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo(400);
                    assertThat(exception.getMessage()).isEqualTo("검색 조건은 필수입니다.");
                });
    }

    @Test
    @DisplayName("지원하지 않는 발송 유형이면 400 예외가 발생한다")
    void getReportHistoryThrowsWhenSendTypeIsInvalid() {
        ReportHistorySearchRequest request = ReportHistorySearchRequest.builder()
                .sendType("email")
                .sendItemType("all")
                .sendStatus("all")
                .page(1)
                .size(10)
                .build();

        assertThatThrownBy(() -> reportHistoryService.getReportHistory(7L, request))
                .isInstanceOfSatisfying(BaseException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo(400);
                    assertThat(exception.getMessage()).isEqualTo("발송 유형은 all, report, webform 중 하나여야 합니다.");
                });
    }

    @Test
    @DisplayName("지원하지 않는 고객 유형이면 400 예외가 발생한다")
    void getReportHistoryThrowsWhenCustomerStageCodeIsInvalid() {
        ReportHistorySearchRequest request = ReportHistorySearchRequest.builder()
                .sendType("all")
                .sendItemType("all")
                .sendStatus("all")
                .customerStageCode("03")
                .page(1)
                .size(10)
                .build();

        assertThatThrownBy(() -> reportHistoryService.getReportHistory(7L, request))
                .isInstanceOfSatisfying(BaseException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo(400);
                    assertThat(exception.getMessage()).isEqualTo("고객 유형은 all, 01, 02 중 하나여야 합니다.");
                });
    }

    @Test
    @DisplayName("고객명 검색어 중간에 공백이 있으면 400 예외가 발생한다")
    void getReportHistoryThrowsWhenKeywordContainsWhitespace() {
        ReportHistorySearchRequest request = ReportHistorySearchRequest.builder()
                .sendType("all")
                .sendItemType("all")
                .sendStatus("all")
                .keyword("김 민준")
                .page(1)
                .size(10)
                .build();

        assertThatThrownBy(() -> reportHistoryService.getReportHistory(7L, request))
                .isInstanceOfSatisfying(BaseException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo(400);
                    assertThat(exception.getMessage()).isEqualTo("고객명 검색어 중간에는 공백을 입력할 수 없습니다.");
                });
    }

    @Test
    @DisplayName("페이지 크기가 100을 초과하면 400 예외가 발생한다")
    void getReportHistoryThrowsWhenPageSizeIsTooLarge() {
        ReportHistorySearchRequest request = ReportHistorySearchRequest.builder()
                .sendType("all")
                .sendItemType("all")
                .sendStatus("all")
                .customerStageCode("all")
                .page(1)
                .size(101)
                .build();

        assertThatThrownBy(() -> reportHistoryService.getReportHistory(7L, request))
                .isInstanceOfSatisfying(BaseException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo(400);
                    assertThat(exception.getMessage()).isEqualTo("페이지 크기는 100 이하여야 합니다.");
                });
    }

    @Test
    @DisplayName("DB 조회 오류가 발생하면 500 예외로 변환한다")
    void getReportHistoryThrowsBaseExceptionWhenMapperFails() {
        ReportHistorySearchRequest request = validRequest();
        given(reportHistoryMapper.countReportHistory(eq(7L), any(ReportHistorySearchRequest.class)))
                .willThrow(new DataAccessResourceFailureException("db error"));

        assertThatThrownBy(() -> reportHistoryService.getReportHistory(7L, request))
                .isInstanceOfSatisfying(BaseException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo(500);
                    assertThat(exception.getMessage()).isEqualTo("발송 내역을 조회하지 못했습니다. 관리자에게 문의하세요.");
                });
    }

    private ReportHistorySearchRequest validRequest() {
        return ReportHistorySearchRequest.builder()
                .sendType("all")
                .sendItemType("all")
                .sendStatus("all")
                .page(1)
                .size(10)
                .build();
    }

    private ReportHistoryItemResponse reportHistoryItem() {
        return ReportHistoryItemResponse.builder()
                .id(1L)
                .sendType("report")
                .sendTypeName("리포트")
                .customerId(100L)
                .customerName("김민준")
                .customerStageCode("02")
                .customerStageName("통합고객")
                .sendItemCode("01")
                .sendItemName("생애주기 성장 리포트")
                .sendStatusCode("02")
                .sendStatusName("발송성공")
                .sentAt(LocalDateTime.of(2026, 7, 1, 14, 30))
                .build();
    }
}
