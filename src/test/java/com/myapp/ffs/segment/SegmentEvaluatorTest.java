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
		boolean evaluate = SegmentEvaluator.evaluate(List.of(c), Logic.AND, Map.of("country", "KR"));
		assertThat(evaluate).isTrue();
	}

	@Test
	@DisplayName("in 세그먼트")
	void in() {
		Condition c = new Condition("deviceOS", Operator.IN, List.of("ios", "Android"));
		boolean evaluate = SegmentEvaluator.evaluate(List.of(c), Logic.AND, Map.of("deviceOS", "ios"));
		assertThat(evaluate).isTrue();
	}

	@Test
	@DisplayName("notEquals 세그먼트")
	void not_equals() {
		Condition c = new Condition("deviceOS", Operator.NOT_EQUALS, "ios");
		boolean evaluate = SegmentEvaluator.evaluate(List.of(c), Logic.AND, Map.of("deviceOS", "ios"));
		assertThat(evaluate).isFalse();
	}

	@Test
	@DisplayName("Logic-or 세그먼트")
	void OrLogic() {
		Condition c1 = new Condition("deviceOS", Operator.EQUALS, "ios");
		Condition c2 = new Condition("country", Operator.IN, List.of("KR", "JP"));
		boolean evaluate = SegmentEvaluator.evaluate(List.of(c1, c2), Logic.OR, Map.of("deviceOS", "ios"));
		assertThat(evaluate).isTrue();
	}

	@Test
	@DisplayName("Logic-and 세그먼트")
	void AndLogic() {
		Condition c1 = new Condition("deviceOS", Operator.EQUALS, "ios");
		Condition c2 = new Condition("country", Operator.IN, List.of("KR", "JP"));
		boolean evaluate = SegmentEvaluator.evaluate(List.of(c1, c2), Logic.AND, Map.of("deviceOS", "ios"));
		assertThat(evaluate).isFalse();
	}

	@Test
	@DisplayName("MATCHES 세그먼트")
	void matches() {
		Condition c = new Condition("userId", Operator.MATCHES, "^dev.*");
		boolean evaluate = SegmentEvaluator.evaluate(List.of(c), Logic.AND, Map.of("userId", "dev123"));
		assertThat(evaluate).isTrue();
	}

	@Test
	@DisplayName("복수조건 세그먼트")
	void multipleConditions() {
		Condition c1 = new Condition("country", Operator.EQUALS, "KR");
		Condition c2 = new Condition("region", Operator.NOT_EQUALS, "EU");
		Condition c3 = new Condition("device", Operator.IN, List.of("iOS", "Android"));

		boolean evaluate = SegmentEvaluator.evaluate(
			List.of(c1, c2, c3),
			Logic.AND,
			Map.of("country", "KR", "region", "Asia", "device", "iOS"));
		assertThat(evaluate).isTrue();
	}

	@Test
	@DisplayName("attribute가 null일 경우")
	void nullAttribute() {
		Condition c = new Condition("country", Operator.EQUALS, "KR");

		boolean evaluate = SegmentEvaluator.evaluate(List.of(c), Logic.AND, Map.of());
		assertThat(evaluate).isFalse();
	}

	@Test
	@DisplayName("In Condition의 value가 List가 아닌 잘못된 타입일 경우")
	void invalidValue() {
		Condition c = new Condition("age", Operator.IN, "notList");

		assertThatThrownBy(() -> SegmentEvaluator.evaluate(List.of(c), Logic.AND, Map.of("age", 20)))
			.isInstanceOf(ClassCastException.class);
	}

}
