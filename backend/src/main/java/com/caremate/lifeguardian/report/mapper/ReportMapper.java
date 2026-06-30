package com.caremate.lifeguardian.report.mapper;

import com.caremate.lifeguardian.report.dto.internal.data.DiseaseRiskItemDto;
import com.caremate.lifeguardian.report.dto.internal.data.DiseaseRiskSummaryDto;
import com.caremate.lifeguardian.report.dto.internal.data.GrowthStandardDto;
import com.caremate.lifeguardian.report.dto.internal.data.ReportContractSummaryDto;
import com.caremate.lifeguardian.report.dto.internal.data.ReportCustomerInfoDto;
import com.caremate.lifeguardian.report.dto.internal.data.ReportWebformDto;
import com.caremate.lifeguardian.report.dto.request.CustomerReportInsertDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportMapper {

    ReportCustomerInfoDto selectReportCustomerInfo(
            @Param("customerId") Long customerId,
            @Param("conversionStatusCode") String conversionStatusCode
    );

    ReportWebformDto selectReportWebform(@Param("webFormId") Long webFormId);

    List<GrowthStandardDto> selectGrowthStandards(
            @Param("gender") String gender,
            @Param("minAge") int minAge,
            @Param("maxAge") int maxAge
    );

    List<DiseaseRiskItemDto> selectDiseaseRisks(
            @Param("ageGroupCode") String ageGroupCode,
            @Param("gender") String gender,
            @Param("treatmentType") String treatmentType,
            @Param("limit") int limit
    );

    List<DiseaseRiskItemDto> selectTopDiseaseRisks(
            @Param("ageGroupCode") String ageGroupCode,
            @Param("gender") String gender,
            @Param("limit") int limit
    );

    List<DiseaseRiskSummaryDto> selectDiseaseRiskSummaries(
            @Param("ageGroupCode") String ageGroupCode,
            @Param("gender") String gender
    );

    List<ReportContractSummaryDto> selectContractSummaries(
            @Param("customerId") Long customerId,
            @Param("limit") int limit
    );

    int countReportTargetBySalesUser(
            @Param("customerId") Long customerId,
            @Param("conversionStatusCode") String conversionStatusCode,
            @Param("salesUserId") Long salesUserId
    );

    int insertCustomerReport(CustomerReportInsertDto report);

    String selectReportLocationForPreview(
            @Param("reportId") Long reportId,
            @Param("currentUserId") Long currentUserId
    );
}
