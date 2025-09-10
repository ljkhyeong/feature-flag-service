package com.myapp.ffs.support;

import static org.assertj.core.api.Assertions.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RulesJsonParserTest {

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
}
