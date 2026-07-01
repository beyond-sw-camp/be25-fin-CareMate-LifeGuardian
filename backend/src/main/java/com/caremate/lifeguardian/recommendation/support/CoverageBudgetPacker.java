package com.caremate.lifeguardian.recommendation.support;

import com.caremate.lifeguardian.recommendation.dto.CoverageCandidateDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class CoverageBudgetPacker {

	public List<CoverageCandidateDto> packBalanced(
			List<CoverageCandidateDto> coverages,
			String mainCategoryCode,
			List<String> subCategoryCodes,
			int maxBudget
	) {
		if (coverages == null || coverages.isEmpty() || maxBudget <= 0) {
			return List.of();
		}

		List<CoverageCandidateDto> selected = new ArrayList<>();
		int usedBudget = 0;

		// 1. 메인 카테고리에서 최고 우선순위 담보 1개 선택
		CoverageCandidateDto mainCoverage =
				findBestCoverageByCategory(
						coverages,
						mainCategoryCode,
						selected
				);

		if (mainCoverage != null && usedBudget + mainCoverage.getUnitPremium() <= maxBudget) {
			selected.add(mainCoverage);
			usedBudget += mainCoverage.getUnitPremium();
		}

		// 2. 서브 카테고리에서 최고 우선순위 담보 1개 선택
		for (String subCategoryCode : subCategoryCodes) {
			CoverageCandidateDto subCoverage =
					findBestCoverageByCategory(
							coverages,
							subCategoryCode,
							selected
					);

			if (subCoverage != null && usedBudget + subCoverage.getUnitPremium() <= maxBudget) {
				selected.add(subCoverage);
				usedBudget += subCoverage.getUnitPremium();

				// 서브 카테고리는 1개만 선택
				break;
			}
		}

		// 3. 남은 예산은 Knapsack으로 최고 점수 조합 선택
		int remainBudget = maxBudget - usedBudget;

		List<CoverageCandidateDto> remainingCoverages =
				coverages.stream()
						.filter(coverage -> !selected.contains(coverage))
						.toList();

		List<CoverageCandidateDto> knapsackSelected =
				packByKnapsack(
						remainingCoverages,
						remainBudget
				);

		selected.addAll(knapsackSelected);

		// 4. 최종 선택 순서 정렬
		selected.sort(
				Comparator.comparingInt(CoverageCandidateDto::getFinalScore)
						.reversed()
						.thenComparingInt(CoverageCandidateDto::getDangerPriorityOrder)
						.thenComparingInt(CoverageCandidateDto::getUnitPremium)
		);

		int selectedOrder = 1;
		for (CoverageCandidateDto coverage : selected) {
			coverage.setSelectedOrder(selectedOrder++);
		}

		return selected;
	}

	private CoverageCandidateDto findBestCoverageByCategory(
			List<CoverageCandidateDto> coverages,
			String categoryCode,
			List<CoverageCandidateDto> selected
	) {
		return coverages.stream()
				.filter(coverage -> coverage.getCategoryCode().equals(categoryCode))
				.filter(coverage -> !selected.contains(coverage))
				.max(
						Comparator.comparingInt(CoverageCandidateDto::getFinalScore)
								.thenComparing(
										Comparator.comparingInt(CoverageCandidateDto::getDangerPriorityOrder)
												.reversed()
								)
				)
				.orElse(null);
	}

	private List<CoverageCandidateDto> packByKnapsack(
			List<CoverageCandidateDto> coverages,
			int maxBudget
	) {
		if (coverages == null || coverages.isEmpty() || maxBudget <= 0) {
			return List.of();
		}

		int n = coverages.size();
		int[][] dp = new int[n + 1][maxBudget + 1];

		for (int i = 1; i <= n; i++) {
			CoverageCandidateDto coverage = coverages.get(i - 1);

			int premium = coverage.getUnitPremium();
			int score = coverage.getFinalScore();

			for (int budget = 0; budget <= maxBudget; budget++) {
				dp[i][budget] = dp[i - 1][budget];

				if (premium <= budget) {
					dp[i][budget] = Math.max(
							dp[i][budget],
							dp[i - 1][budget - premium] + score
					);
				}
			}
		}

		List<CoverageCandidateDto> selected = new ArrayList<>();

		int budget = maxBudget;

		for (int i = n; i >= 1; i--) {
			CoverageCandidateDto coverage = coverages.get(i - 1);

			if (dp[i][budget] != dp[i - 1][budget]) {
				selected.add(coverage);
				budget -= coverage.getUnitPremium();
			}
		}

		return selected;
	}
}