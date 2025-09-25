package com.myapp.ffs.segment;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class OperatorStrategy {
	private static final Map<Operator, BiFunction<Condition, Map<String, Object>, Boolean>> STRATEGIES = Map.of(
		Operator.EQUALS, (cond, attrs) -> attrs.getOrDefault(cond.attribute(), "").equals(cond.value()),
		Operator.NOT_EQUALS, (cond, attrs) -> !attrs.getOrDefault(cond.attribute(), "").equals(cond.value()),
		Operator.IN, (cond, attrs) -> ((List<?>)cond.value()).contains(attrs.getOrDefault(cond.attribute(), "")),
		Operator.NOT_IN, (cond, attrs) -> !((List<?>)cond.value()).contains(attrs.getOrDefault(cond.attribute(), "")),
		Operator.MATCHES, (cond, attrs) -> attrs.getOrDefault(cond.attribute(), "").toString().matches(cond.value().toString())
		);

	public static boolean evaluate(Condition condition, Map<String, Object> attributes) {
		return STRATEGIES.get(condition.operator()).apply(condition, attributes);
	}



}
