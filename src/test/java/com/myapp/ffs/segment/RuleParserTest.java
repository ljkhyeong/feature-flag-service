package com.myapp.ffs.segment;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
