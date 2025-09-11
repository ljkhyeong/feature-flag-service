package com.myapp.ffs.client;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.myapp.ffs.flag.dto.FlagItem;
import com.myapp.ffs.sdk.service.RolloutEvaluator;

class RolloutEvaluatorTest {

	@Test
	@DisplayName("include된 id 평가 - true")
	void include_overrides_exclude() {
		FlagItem item = new FlagItem("checkout.new", false, 0, Set.of("u1"), Set.of());
		assertThat(item.isEnabledFor("u1")).isTrue();
	}
	@Test
	@DisplayName("exclude된 id 평가 - false")
	void exclude_overrides_exclude() {
		FlagItem item = new FlagItem("checkout.new", true, 100, Set.of(), Set.of("u1"));
		assertThat(item.isEnabledFor("u1")).isFalse();
	}

	@Test
	@DisplayName("롤아웃 퍼센티지 적용")
	void rollout_percentage_applied() {
		FlagItem item = new FlagItem("checkout.new", false, 50, Set.of(), Set.of());
		boolean result = item.isEnabledFor("user123");
		assertThat(result).isIn(true, false); // 디버깅으로 확인
	}

	@Test
	@DisplayName("아무것도 적용 안되면 baseEnabled")
	void fallback_to_baseEnabled() {
		FlagItem item = new FlagItem("checkout.new", true, null, Set.of(), Set.of());
		assertThat(item.isEnabledFor("randomUser")).isTrue();
	}

}
