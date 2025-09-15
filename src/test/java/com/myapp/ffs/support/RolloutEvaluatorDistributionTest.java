package com.myapp.ffs.support;

import static org.assertj.core.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import com.myapp.ffs.sdk.service.RolloutEvaluator;

public class RolloutEvaluatorDistributionTest {

	@RepeatedTest(3)
	@DisplayName("롤아웃 퍼센티지 분포가 허용 오차(+-1) 내")
	void distributionAccuracy() {
		int rolloutPercentage = 30;
		int sampleSize = 10_000;

		Set<String> include = new HashSet<>();
		Set<String> exclude = new HashSet<>();

		long enabledCount = IntStream.range(0, sampleSize)
			.mapToObj(i -> "user-" + i)
			.filter(userId -> RolloutEvaluator.isEnabledForUser(
				userId,
				false,
				rolloutPercentage,
				include,
				exclude
			))
			.count();

		double actualRatio = (enabledCount * 100.0) / sampleSize;

		assertThat(actualRatio)
			.as("분포 비율이 rolloutPercentage ±1% 범위에 들어와야 함")
			.isBetween(rolloutPercentage - 1.0, rolloutPercentage + 1.0);
	}


}
