package com.myapp.ffs.segment;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SegmentEvaluator {
	public static boolean evaluate(List<Condition> conditions, Logic logic, Map<String, Object> attributes) {
		boolean result = (logic == Logic.AND);

		for (Condition condition : conditions) {
			boolean match = OperatorStrategy.evaluate(condition, attributes);

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
