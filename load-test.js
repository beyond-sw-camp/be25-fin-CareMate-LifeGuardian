import http from 'k6/http';
import { check, sleep } from 'k6';

// k6 부하 테스트 옵션 정의 (점진적 부하 증가 및 유지 시나리오)
export const options = {
  insecureSkipTLSVerify: true,
  stages: [
    { duration: '10s', target: 20000 }, // 10초 만에 20,000명으로 급격히 증가 (우르르 들어옴)
    { duration: '1m', target: 20000 },  // 1분 동안 20,000명 동시 접속 상태 유지
    { duration: '10s', target: 0 },     // 10초 만에 0명으로 급격히 감소 (나감)
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],   // 에러율 1% 미만이어야 성공
    http_req_duration: ['p(95)<1000'], // 95%의 요청이 1초(1000ms) 이내에 응답해야 함
  },
};

// ----------------------------------------------------
// 테스트 대상 서버의 기본 URL (k6 실행 시 환경 변수로 주입 가능)
// 예: k6 run -e TARGET_URL=http://lifeguardian-alb-1734347245.ap-northeast-2.elb.amazonaws.com  load-test.js
// ----------------------------------------------------
const BASE_URL = __ENV.TARGET_URL || 'http://lifeguardian-alb-1734347245.ap-northeast-2.elb.amazonaws.com';

export default function () {
  // 1. CPU 부하 테스트 API 호출 (SHA-256 연산 반복)
  let cpuRes = http.get(`${BASE_URL}/api/test/load/cpu?iterations=50000`);
  check(cpuRes, {
    'CPU 부하 API: 상태코드 200': (r) => r.status === 200,
    'CPU 부하 API: durationMs 필드 포함 여부': (r) => r.json().hasOwnProperty('durationMs'),
  });

  sleep(0.5); // 요청 간 짧은 대기 시간 (0.5초)

  // 2. DB 부하 테스트 API 호출 (SELECT 1 10회 반복 수행)
  let dbRes = http.get(`${BASE_URL}/api/test/load/db?count=10`);
  check(dbRes, {
    'DB 부하 API: 상태코드 200': (r) => r.status === 200,
  });

  sleep(0.5);

  // 3. 스레드 지연 부하 테스트 API 호출 (500ms 지연)
  let delayRes = http.get(`${BASE_URL}/api/test/load/delay?ms=500`);
  check(delayRes, {
    '지연 부하 API: 상태코드 200': (r) => r.status === 200,
  });

  sleep(1); // 다음 루프 실행 전 대기
}
