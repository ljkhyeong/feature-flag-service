package com.myapp.ffs.sdk.evaluator;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.myapp.ffs.flag.dto.FlagItem;
import com.myapp.ffs.sdk.service.RolloutEvaluator;

public class RolloutEvaluatorSpecTest {
	@Test
	@DisplayName("include 우선순위 = 1")
	void include() {
		assertThat(RolloutEvaluator
			.isEnabledForUser("u1", false, 0, Set.of("u1"), Set.of()))
			.isTrue();
	}
	@Test
	@DisplayName("exclude 우선순위 = 2")
	void exclude() {
		assertThat(RolloutEvaluator
			.isEnabledForUser("u1", true, 100, Set.of(), Set.of("u1")))
			.isFalse();
	}

	@Test
	@DisplayName("롤아웃 퍼센티지 우선순위 = 3")
	void rollout_percentage() {
		assertThat(RolloutEvaluator
			.isEnabledForUser("user123", true, 0, Set.of(), Set.of()))
			.isFalse();
		assertThat(RolloutEvaluator
			.isEnabledForUser("user123", false, 100, Set.of(), Set.of()))
			.isTrue();
	}
	@Test
	@DisplayName("baseEnabled 우선순위 = 4")
	void rollout_percentage_applied() {
		assertThat(RolloutEvaluator
			.isEnabledForUser("user123", true, null, Set.of(), Set.of()))
			.isTrue();
	}
}
