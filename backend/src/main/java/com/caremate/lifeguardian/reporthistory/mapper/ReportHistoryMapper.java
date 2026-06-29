package com.caremate.lifeguardian.reporthistory.mapper;

import com.caremate.lifeguardian.reporthistory.dto.request.ReportHistorySearchRequest;
import com.caremate.lifeguardian.reporthistory.dto.response.ReportHistoryItemResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportHistoryMapper {

    long countReportHistory(
            @Param("currentUserId") Long currentUserId,
            @Param("request") ReportHistorySearchRequest request
    );

    List<ReportHistoryItemResponse> findReportHistory(
            @Param("currentUserId") Long currentUserId,
            @Param("request") ReportHistorySearchRequest request
    );
}
