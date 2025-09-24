package com.myapp.ffs.segment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.ffs.exception.ApplicationException;
import com.myapp.ffs.exception.ErrorCode;

public class RuleParser {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static ParseRuleSet parseWithLogic(String json) throws IOException {
		JsonNode root = objectMapper.readTree(json);
		List<Condition> conditions = parse(json);
		Logic logic = root.has("logic") ? Logic.valueOf(root.get("logic").asText()) : Logic.AND;

		return new ParseRuleSet(conditions, logic);
	}

	public static List<Condition> parse(String json) throws IOException {
		JsonNode root = objectMapper.readTree(json);

		List<Condition> conditions = new ArrayList<>();
		Iterator<JsonNode> iterator = root.get("conditions").elements();
		while (iterator.hasNext()) {
			JsonNode node = iterator.next();

			if (!node.has("attribute") || !node.has("operator") || !node.has("value")) {
				throw new ApplicationException(ErrorCode.INVALID_CONDITION_JSON);
			}

			String attribute = node.get("attribute").asText();
			Operator operator;

			try {
				operator = Operator.valueOf(node.get("operator").asText()); // Null Pointer 발생 가능, 수정필요
			} catch (IllegalArgumentException e) {
				throw new ApplicationException(ErrorCode.UNSUPPORTED_OPERATOR);
			}
			JsonNode val = node.get("value");

			Object value;
			if (val.isArray()) {
				List<String> list = new ArrayList<>();
				val.forEach(v -> list.add(v.asText()));
				value = list;
			} else {
				value = val.asText();
			}

			conditions.add(new Condition(attribute, operator, value));
		}
		return conditions;
	}
}
