package com.caremate.lifeguardian.webformPage.mapper;

import com.caremate.lifeguardian.recommendation.domain.WebformResponse;
import com.caremate.lifeguardian.webformPage.dto.WebformIssuanceDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WebformPageMapper {

    /**
     * 웹폼 문진표 제출 답변 데이터를 저장한다.
     *
     * @param response 웹폼 답변 도메인 객체
     */
    void insertWebformResponse(WebformResponse response);

    /**
     * 전역 잠재고객 존재 여부 확인
     */
    boolean existsPotentialCustomerById(@Param("customerId") Long customerId);

    /**
     * 전역 통합고객 존재 여부 확인
     */
    boolean existsIntegratedCustomerById(@Param("customerId") Long customerId);

    /**
     * 전역 잠재고객 이름 조회
     */
    String findPotentialCustomerNameById(@Param("customerId") Long customerId);

    /**
     * 전역 통합고객 이름 조회
     */
    String findIntegratedCustomerNameById(@Param("customerId") Long customerId);

    /**
     * UUID 토큰으로 웹폼 발송 이력 상세 조회
     */
    WebformIssuanceDto findIssuanceByToken(@Param("token") String token);
}
