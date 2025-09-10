package com.myapp.ffs.support;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RulesJsonParser {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static Set<String> parseInclude(String rulesJson) {
		return parseArray(rulesJson, "include");
	}

	public static Set<String> parseExclude(String rulesJson) {
		return parseArray(rulesJson, "exclude");
	}

	private static Set<String> parseArray(String rulesJson, String field) {
		Set<String> result = new LinkedHashSet<>();
		if (rulesJson == null || rulesJson.isBlank()) {
			return result;
		}
		try {
			JsonNode root = objectMapper.readTree(rulesJson);
			JsonNode arr = root.get(field);
			if (arr == null || arr.isNull()) {
				return result;
			}

			if (arr.isArray()) {
				for (JsonNode node : arr) {
					if (node.isTextual()) {
						result.add(node.asText());
					}
				}
			} else if (arr.isTextual()) {
				for (String str : arr.asText().split(",")) {
					String s = str.trim();
					if (!s.isEmpty()) {
						result.add(s);
					}
				}
			}
		} catch (JsonMappingException e) {

		} catch (JsonProcessingException e) {
		}
		return result;
	}
}
