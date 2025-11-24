package com.myapp.ffs.segment;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.myapp.ffs.exception.ApplicationException;
import com.myapp.ffs.exception.ErrorCode;
import com.myapp.ffs.support.RulesJsonParser;

class RuleParserTest {

	@Test
	@DisplayName("include, exclude 올바른 배열 파싱")
	void parse_include_exclude_arrays() {
		String json = """
			{ 	"include": ["u1","u2"],
				"exclude": ["u9"] }
			""";

		assertThat(RulesJsonParser.parseInclude(json)).containsExactlyInAnyOrder("u1", "u2");
		assertThat(RulesJsonParser.parseExclude(json)).containsExactly("u9");
	}

	@Test
	@DisplayName("올바르지 않은 배열 파싱 (콤마, 공란 포함)")
	void parse_include_exclude_arrays_with_comma() {
		String json = """
			{ 	"include": "a,b , c", 
				"exclude": null }
			""";

		assertThat(RulesJsonParser.parseInclude(json)).containsExactlyInAnyOrder("a", "b", "c");
		assertThat(RulesJsonParser.parseExclude(json)).isEmpty();
	}

	@Test
	@DisplayName("올바르지 않은 json 파싱")
	void parse_invalid_json() {
		String json = """
			{ invalid Json ...
			""";

		assertThat(RulesJsonParser.parseInclude(json)).isEmpty();
		assertThat(RulesJsonParser.parseExclude(json)).isEmpty();
	}

	@Test
	@DisplayName("conditions json 파싱")
	void parseEqualsConditions() throws Exception {
		String json = """
        {
          "conditions": [
            { "attribute": "country", "operator": "EQUALS", "value": "KR" }
          ]
        }
        """;

		List<Condition> conditions = RuleParser.parse(json);

		assertThat(conditions.size()).isEqualTo(1);
		assertThat(conditions.get(0).attribute()).isEqualTo("country");
		assertThat(conditions.get(0).operator()).isEqualTo(Operator.EQUALS);
		assertThat(conditions.get(0).value()).isEqualTo("KR");
	}

	@Test
	@DisplayName("파싱 후 평가")
	void parseAndEvaluate() throws Exception {
		String json = """
		{
		  "conditions": [
			{ "attribute": "country", "operator": "EQUALS", "value": "KR" }
		  ],
		  "logic": "AND"
		}
		""";

		ParseRuleSet parsed = RuleParser.parseWithLogic(json);

		boolean result = SegmentEvaluator.evaluate(parsed.conditions(), parsed.logic(), Map.of("country", "KR"));
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("잘못된 JSON 형태")
	void invalidJson() {
		String json = """
			{
			  "conditions": [
				{ "attribute": "country", "value": "KR" }
			  ]
			}
			""";

		assertThatThrownBy(() -> RuleParser.parseWithLogic(json))
			.isInstanceOf(ApplicationException.class)
			.hasMessage(ErrorCode.INVALID_CONDITION_JSON.getMessage());
	}

	@Test
	@DisplayName("logic 없으면 기본값 AND ")
	void without_logic() throws Exception {
		String json = """
		{
		  "conditions": [
			{ "attribute": "country", "operator": "EQUALS", "value": "KR" }
		  ]
		}
		""";

		ParseRuleSet parsed = RuleParser.parseWithLogic(json);

		boolean result = SegmentEvaluator.evaluate(parsed.conditions(), parsed.logic(), Map.of("country", "KR"));
		assertThat(result).isTrue();
	}
	@Test
	@DisplayName("유효하지 않은 operator")
	void invalid_operator() throws Exception {
		String json = """
		{
		  "conditions": [
			{ "attribute": "country", "operator": "BETWEEN", "value": "KR" }
		  ]
		}
		""";

		assertThatThrownBy(() -> RuleParser.parseWithLogic(json))
			.isInstanceOf(ApplicationException.class)
			.hasMessageContaining(ErrorCode.UNSUPPORTED_OPERATOR.getMessage());
	}
}
