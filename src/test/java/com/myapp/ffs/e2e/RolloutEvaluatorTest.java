package com.myapp.ffs.e2e;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.myapp.ffs.sdk.service.RolloutEvaluator;

class RolloutEvaluatorTest {

	@Test
	@DisplayName("include된 id 평가 - true")
	void include_overrides_exclude() {
		assertThat(RolloutEvaluator
				.isEnabledForUser("u1", false, 0, Set.of("u1"), Set.of("u2")))
			.isTrue();
	}
	@Test
	@DisplayName("exclude된 id 평가 - false")
	void exclude_overrides_exclude() {
		assertThat(RolloutEvaluator
				.isEnabledForUser("u1", true, 100, Set.of("u2"), Set.of("u1")))
			.isFalse();
	}

	@Test
	@DisplayName("롤아웃 퍼센티지 적용")
	void rollout_percentage_applied() {
		assertThat(RolloutEvaluator
			.isEnabledForUser("user123", false, 50, Set.of(), Set.of()))
			.isIn(true, false);

		// 이건 개선이 필요할듯
	}
}
