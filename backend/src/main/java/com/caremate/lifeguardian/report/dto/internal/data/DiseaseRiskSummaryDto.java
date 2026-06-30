package com.caremate.lifeguardian.report.dto.internal.data;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DiseaseRiskSummaryDto")
public class DiseaseRiskSummaryDto {

    private String dataYear;
    private String treatmentType;
    private String treatmentTypeName;
    private Integer diseaseCount;
    private Integer totalPatientCount;
    private Integer maxPatientCount;
    private Integer barWidthPercent;
}
