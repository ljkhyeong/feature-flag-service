package com.myapp.ffs.segment;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SegmentEvaluatorTest {

	@Test
	@DisplayName("equals 세그먼트")
	void equals() {
		Condition c = new Condition("country", Operator.EQUALS, "KR");
		boolean evaluate = SegmentEvaluator.evaluate(List.of(c), "AND", Map.of("country", "KR"));
		assertThat(evaluate).isTrue();
	}
	@Test
	@DisplayName("in 세그먼트")
	void in() {
		Condition c = new Condition("deviceOS", Operator.IN, List.of("ios", "Android"));
		boolean evaluate = SegmentEvaluator.evaluate(List.of(c), "AND", Map.of("deviceOS", "ios"));
		assertThat(evaluate).isTrue();
	}
}
