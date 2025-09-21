package com.myapp.ffs.segment;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SegmentEvaluator {
	public static boolean evaluate(List<Condition> conditions, String logic, Map<String, Object> attributes) {
		boolean result = logic.equalsIgnoreCase("AND");

		for (Condition condition : conditions) {
			boolean match = switch (condition.operator()) {
				case EQUALS -> attributes.getOrDefault(condition.attribute(), "").equals(condition.value());
				case NOT_EQUALS -> !attributes.getOrDefault(condition.attribute(), "").equals(condition.value());
				case IN -> ((List<?>)condition.value()).contains(attributes.get(condition.attribute()));
				case NOT_IN -> !((List<?>)condition.value()).contains(attributes.get(condition.attribute()));
				case MATCHES ->
					attributes.getOrDefault(condition.attribute(), "").toString().matches(condition.value().toString());
			};

			if ("AND".equalsIgnoreCase(logic)) {
				result = result && match;
			}
			if ("OR".equalsIgnoreCase(logic)) {
				result = result || match;
			}
		}
		return result;
	}
}
