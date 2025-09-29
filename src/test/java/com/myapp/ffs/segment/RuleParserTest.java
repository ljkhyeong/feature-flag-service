package com.myapp.ffs.segment;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.myapp.ffs.exception.ApplicationException;
import com.myapp.ffs.exception.ErrorCode;

class RuleParserTest {


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
