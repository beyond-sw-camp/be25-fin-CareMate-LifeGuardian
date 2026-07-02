package com.caremate.lifeguardian.common.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test/load")
@RequiredArgsConstructor
public class LoadTestController {

    private final JdbcTemplate jdbcTemplate;

    // 1. CPU 부하 테스트 API (SHA-256 루프 연산)
    @GetMapping("/cpu")
    public ResponseEntity<Map<String, Object>> cpuLoad(
            @RequestParam(value = "iterations", defaultValue = "50000") int iterations) {
        
        long startTime = System.currentTimeMillis();
        String hashResult = "test-data-for-load";
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (int i = 0; i < iterations; i++) {
                byte[] hash = digest.digest(hashResult.getBytes());
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                hashResult = hexString.toString();
            }
        } catch (NoSuchAlgorithmException e) {
            log.error("Hash algorithm not found", e);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        
        Map<String, Object> result = new HashMap<>();
        result.put("type", "CPU_LOAD");
        result.put("iterations", iterations);
        result.put("durationMs", duration);
        result.put("resultHash", hashResult);
        
        return ResponseEntity.ok(result);
    }

    // 2. DB 부하 테스트 API (간단한 SELECT 쿼리 다중 반복 수행)
    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> dbLoad(
            @RequestParam(value = "count", defaultValue = "10") int count) {
        
        long startTime = System.currentTimeMillis();
        
        // SELECT 1 쿼리를 count 횟수만큼 반복 호출하여 커넥션 풀 및 DB 부하 유도
        for (int i = 0; i < count; i++) {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        
        Map<String, Object> result = new HashMap<>();
        result.put("type", "DB_LOAD");
        result.put("queryCount", count);
        result.put("durationMs", duration);
        
        return ResponseEntity.ok(result);
    }

    // 3. 지연 부하 테스트 API (Thread Sleep을 통한 지연 발생 및 스레드 풀 모니터링)
    @GetMapping("/delay")
    public ResponseEntity<Map<String, Object>> delayLoad(
            @RequestParam(value = "ms", defaultValue = "500") int ms) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread sleep interrupted", e);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        
        Map<String, Object> result = new HashMap<>();
        result.put("type", "DELAY_LOAD");
        result.put("requestedDelayMs", ms);
        result.put("actualDurationMs", duration);
        
        return ResponseEntity.ok(result);
    }
}
