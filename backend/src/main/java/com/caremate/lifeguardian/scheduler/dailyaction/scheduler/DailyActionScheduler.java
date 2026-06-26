package com.caremate.lifeguardian.scheduler.dailyaction.scheduler;

import com.caremate.lifeguardian.scheduler.dailyaction.service.DailyActionBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DailyActionScheduler {

	private final DailyActionBatchService dailyActionBatchService;

	// 매일 새벽 2시
	@Scheduled(cron = "0 0 0 * * *")
	@SchedulerLock(
			name = "createDailyActionItemsLock",
			lockAtLeastFor = "1m",
			lockAtMostFor = "10m"
	)
	public void createDailyActionItems() {
		dailyActionBatchService.createTodayActionItems(LocalDate.now());
	}

	// 보정 배치: 오전 8시 30분
	@Scheduled(cron = "0 30 8 * * *")
	@SchedulerLock(
			name = "retryTodayActionItemsLock",
			lockAtLeastFor = "1m",
			lockAtMostFor = "10m"
	)
	public void retryTodayActionItems() {
		dailyActionBatchService.createTodayActionItems(LocalDate.now());
	}
}