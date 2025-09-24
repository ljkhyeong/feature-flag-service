package com.myapp.ffs.segment;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SegmentEvaluator {
	public static boolean evaluate(List<Condition> conditions, Logic logic, Map<String, Object> attributes) {
		boolean result = (logic == Logic.AND);

		for (Condition condition : conditions) {
			boolean match = switch (condition.operator()) {
				case EQUALS -> attributes.getOrDefault(condition.attribute(), "").equals(condition.value());
				case NOT_EQUALS -> !attributes.getOrDefault(condition.attribute(), "").equals(condition.value());
				case IN -> ((List<?>)condition.value()).contains(attributes.getOrDefault(condition.attribute(), "")); // condition.attribute가 널일수있음. 나중에 리팩토링하자
				case NOT_IN -> !((List<?>)condition.value()).contains(attributes.getOrDefault(condition.attribute(), ""));
				case MATCHES ->
					attributes.getOrDefault(condition.attribute(), "").toString().matches(condition.value().toString());
			};

			if (logic == Logic.AND) {
				result = result && match;
			}
			if (logic == Logic.OR) {
				result = result || match;
			}
		}
		return result;
	}
}
