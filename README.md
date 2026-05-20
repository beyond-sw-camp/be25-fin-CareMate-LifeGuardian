# LifeGuardian 

<img width="1000" alt="LifeGuardian _썸네일" src="..." />

## 👥 팀원 소개

<table style="width:100%; text-align:center;">
  <thead>
    <tr>
      <th>윤준상</th>
      <th>김다솜</th>
      <th>박하얀</th>
      <th>박재하</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>
        <img src="..." style="width:120px; height:140px; object-fit:contain;"><br>
        🔗 <a href="https://github.com/wnstkd704">wnstkd704</a>
      </td>
      <td>
        <img src="..." style="width:120px; height:140px; object-fit:contain;"><br>
        🔗 <a href="https://github.com/myangD">myangD</a>
      </td>
      <td>
        <img src="..." style="width:120px; height:140px; object-fit:contain;"><br>
        🔗 <a href="https://github.com/P-HAYAN">P-HAYAN</a>
      </td>
      <td>
        <img src="..." style="width:120px; height:140px; object-fit:contain;"><br>
        🔗 <a href="https://github.com/horolo1234">horolo1234</a>
      </td>
    </tr>
  </tbody>
</table>

<br>

## 📍 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [배경 및 기획의도](#2-배경-및-기획의도)
3. [WBS](#3-WBS)
4. [기술 스택](#4-기술-스택)  
5. [시스템 아키텍처](#5-시스템-아키텍처)
6. [프로젝트 구조](#6-프로젝트-구조)
7. [요구사항 정의서](#7-요구사항-정의서)  
8. [테이블 정의서](#8-테이블-정의서)  
9. [ERD](#9-ERD)
10. [화면 및 기능 설계서](#10-화면-및-기능-설계서)
11. [API 명세서](#11-API-명세서)  
12. [테스트 보고서](#12-테스트-보고서)
13. [회고](#13-회고)

<br>

## <a id="1-프로젝트-개요"></a> 1. 프로젝트 개요 

LifeGuardian은 보험사가 보유한 0~20세 자녀 가망고객 데이터를 활용하여, 보험 영업사원이 고객의 생애주기(건강·성장) 데이터를 기반으로 잠재 고객을 발굴하고, 고객별 보장 공백과 상담 타이밍을 분석하여 추천 보험 카테고리·상담 스크립트·우선 연락 대상을 제공하는 데이터 기반 보험 세일즈 큐레이션 플랫폼입니다.
단순 고객관리에 그치지 않고, 가망고객 배정 → 고객정보 수집 → 맞춤 보험 카테고리 추천 → 상담 스크립트 제공 → 계약 결과 관리 → 실패 고객 재공략 판단까지 지원합니다.
- 🗒️[기획서 (링크)](https://docs.google.com/document/d/1Kw0PjYCBg7ok991l6l2LZpR-QU-j9Hm67TgT6RC7wrI/edit?tab=t.0)

<br>

## <a id="2-배경-및-기획의도"></a> 2. 배경 및 기획의도

도입 배경: 가망고객 관리의 한계와 체계적 전환의 필요성 
현재 보험 영업 시장은 신규 고객 유치가 점차 어려워짐에 따라, 이미 확보한 가망고객 데이터를 실제 상담과 계약으로 전환하는 체계적인 관리 시스템의 중요성이 대두되고 있습니다. 하지만 기존의 영업 방식은 고객별 보장 공백이나 최적의 상담 타이밍, 자녀의 성장 단계, 과거 상담 이력 등이 데이터로 관리되지 않아 전적으로 영업사원 개인의 판단과 역량에 의존하는 한계가 있습니다. 이로 인해 귀중한 가망고객이 단순한 연락 대상에 머무르고, 상담 성공률이 높은 고객이나 실패 고객에 대한 재공략 기회를 번번이 놓치는 비효율이 발생하고 있습니다.

<br>
기획 의도: 자녀 보험을 매개로 한 가족 통합 세일즈 기회 창출 
본 프로젝트는 이러한 한계를 극복하기 위해 '0~20세 자녀를 둔 가망고객'의 특성에 주목했습니다. 자녀 보험의 실질적인 의사결정권과 결제권은 전적으로 '부모'에게 있습니다. 따라서 자녀의 성장 단계와 객관적인 보장 필요성을 근거로 접근하면, 부모와의 자연스러운 상담 창구를 열 수 있습니다.
더 나아가 이 상담 기회를 '도어 오프너(Door-opener)'로 활용하여 부모의 연령 변화와 기존 보장 상태까지 함께 점검한다면, 단건의 자녀 보험 상담을 객단가가 높은 '가족 단위 통합 보험 리모델링' 기회로 자연스럽게 확장할 수 있습니다.
결과적으로 본 플랫폼은 잠들어 있는 가망고객 데이터를 기반으로 자녀 보험 상담의 적기를 시스템이 자동으로 포착하고, 이를 부모의 보장 점검까지 유기적으로 연결하여 가망고객을 '가족 단위 통합 우수 고객'으로 전환하는 세일즈 큐레이션 환경을 제공하고자 기획되었습니다.

<br>

## <a id="3-WBS"></a> 3. WBS

<details>
<summary><b>🗓️ WBS 링크 </b></summary>

- 🗓️ [WBS (링크)](https://docs.google.com/spreadsheets/d/1jCL1br1RoIoiYlqEJTS_a4W8B9hhZFSuSCLUUObTIzE/edit?gid=503263989#gid=503263989)

</details>
<br>

## <a id="4-기술-스택"></a> 4. 기술 스택



<br>

## <a id="5-시스템-아키텍처"></a> 5. 시스템 아키텍처

<details>
<summary><b>🧱 시스템 아키텍처 펼쳐보기</b></summary>

<img src="..." width="1000" alt="ERD image" /></br>

</details>
<br>


## <a id="6-프로젝트-구조"></a> 6. 프로젝트 구조

<details>
<summary><b>📁 폴더 구조 펼쳐보기</b></summary>

```txt

```
</details>
<br/><br/>



## <a id="7-요구사항-정의서"></a> 7. 요구사항 정의서

<details>
<summary><b>🗒️ 요구사항 정의서 링크</b></summary>

- 🗒️[요구사항 정의서 (링크)](https://docs.google.com/spreadsheets/d/1jCL1br1RoIoiYlqEJTS_a4W8B9hhZFSuSCLUUObTIzE/edit?gid=182532999#gid=182532999)
</details>
<br>


## <a id="8-테이블-정의서"></a> 8. 테이블 정의서

<details>
<summary><b>🗄️ 테이블 정의서</b></summary>
  
- 🗄️[테이블 정의서 (링크)](https://docs.google.com/spreadsheets/d/1jCL1br1RoIoiYlqEJTS_a4W8B9hhZFSuSCLUUObTIzE/edit?gid=1700824556#gid=1700824556)

</details>
<br>

## <a id="9-ERD"></a> 9. ERD

<details>
<summary><b>📌 ERD 구조도</b></summary>
  
- [📌 ERD 구조도 (링크)](http...)
  
</details>
<br>

## <a id="10-화면-및-기능-설계서"></a> 10. 화면 및 기능 설계서

<details>
<summary><b>📱 화면기능 설계서 링크</b></summary>
  
- [📱 화면기능 설계서 (링크)](http...)

</details>
<br>

## <a id="11-API-명세서"></a> 11. API 명세서

<details>
<summary><b>📋 API 명세서 링크</b></summary>
  
- [📋 API 명세서 (링크)](http...)

</details>
<br>

## <a id="12-테스트-보고서"></a> 12. 테스트 보고서(스프레드 시트)

<details>
 <summary><b>🧾 백엔드 테스트</b></summary>

- [🧾 백엔드 테스트 결과서 (링크)](http...)

</details>
<br>

## <a id="13-회고"></a> 13. 회고

|   이름   |     회고 내용     |
|-----------|-----------------|
|      윤준상      |     |
|      김다솜      |     |
|      박하연      |     |
|      박재하      |     |

<br>
