package com.caremate.lifeguardian.esg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class EsgTestController {

    private final EsgBatchScheduler esgBatchScheduler;

    @GetMapping("/run-esg-batch")
    public String runEsgBatch() {
        log.info("Manual trigger of ESG Batch job via REST API.");
        try {
            esgBatchScheduler.runEsgDataAccumulationJob();
            return "ESG Batch Job triggered successfully. Check logs for details.";
        } catch (Exception e) {
            log.error("Failed to run ESG Batch manually.", e);
            return "Failed to trigger ESG Batch: " + e.getMessage();
        }
    }
}
