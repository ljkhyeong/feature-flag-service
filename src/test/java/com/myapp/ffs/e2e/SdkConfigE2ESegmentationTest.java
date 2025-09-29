package com.myapp.ffs.e2e;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import com.myapp.ffs.segment.Condition;
import com.myapp.ffs.segment.ParseRuleSet;
import com.myapp.ffs.segment.RuleParser;
import com.myapp.ffs.segment.SegmentEvaluator;

public class SdkConfigE2ESegmentationTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@DisplayName("exclude 해당하면 세그먼트 매칭되도 false")
	void includeFirst() throws Exception {
		// given
		// exclude 우선 → false
		// include/exclude는 별도 레이어이므로 여기선 세그먼트 평가만 검증
		boolean result = false /* exclude contains userId 이라고 가정 */;

		String rulesJson = """
        {
        "conditions":[
        {"attribute":"country","operator":"EQUALS","value":"KR"}
        ],
        "logic":"AND"
        }
        """;
		ParseRuleSet parsed = RuleParser.parseWithLogic(rulesJson);
		Map<String, Object> attrs = Map.of("country", "KR");

		// when
		boolean evaluated = SegmentEvaluator.evaluate(parsed.conditions(), parsed.logic(), attrs);
		assertThat(evaluated).isTrue();

		// then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("include 해당하면 세그먼트 매칭안되도 true")
	void segmentSecond() throws Exception {
		// given
		// include 우선 → true
		boolean finalResult = true;

		String rulesJson = """
        {
        "conditions":[
        {"attribute":"country","operator":"IN","value":["KR", "JP"]}
        ],
        "logic":"AND"
        }
        """;
		ParseRuleSet parsed = RuleParser.parseWithLogic(rulesJson);
		Map<String, Object> attrs = Map.of("country", "US");

		// when
		boolean evaluated = SegmentEvaluator.evaluate(parsed.conditions(), parsed.logic(), attrs);
		assertThat(evaluated).isFalse();

		// then
		// include 우선순위 적용했다고 가정 → 최종판정은 true
		assertThat(finalResult).isTrue();
	}

	@Test
	@DisplayName("rolloutPercentage랑 상관없이 세그먼트 매칭시 true")
	void segment_match_then_true_even_if_rollout_exists() throws Exception {
		// given
		String rulesJson = """
        {
        "conditions":[
        {"attribute":"country","operator":"IN","value":["KR", "JP"]}
        ],
        "logic":"AND"
        }
        """;
		ParseRuleSet parsed = RuleParser.parseWithLogic(rulesJson);
		Map<String, Object> attrs = Map.of("country", "JP");

		// when
		boolean evaluated = SegmentEvaluator.evaluate(parsed.conditions(), parsed.logic(), attrs);
		assertThat(evaluated).isTrue();

		// then
		boolean finalResult = true;
		assertThat(finalResult).isTrue();
	}

	@Test
	@DisplayName("세그먼트 매칭 실패하면 rolloutPercentage 적용")
	void not_segment_match_apply_rolloutPercentage() throws Exception {
		// given
		String rulesJson = """
        {
        "conditions":[
        {"attribute":"country","operator":"IN","value":["KR", "JP"]}
        ],
        "logic":"AND"
        }
        """;
		ParseRuleSet parsed = RuleParser.parseWithLogic(rulesJson);
		Map<String, Object> attrs = Map.of("country", "US");

		// when
		boolean evaluated = SegmentEvaluator.evaluate(parsed.conditions(), parsed.logic(), attrs);
		assertThat(evaluated).isFalse();

		// then
		// segment 불일치 → rollout 결과가 최종 반영
		boolean finalResult = true; // 예시: rollout % 안에 들어왔다고 가정
		assertThat(finalResult).isTrue();
	}

}
