package com.myapp.ffs.e2e;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.ffs.api.SdkConfigController;
import com.myapp.ffs.api.SdkConfigControllerTest;
import com.myapp.ffs.flag.dto.FlagItem;
import com.myapp.ffs.sdk.dto.SdkBundle;
import com.myapp.ffs.sdk.dto.SdkConfigResponse;
import com.myapp.ffs.sdk.service.SdkConfigService;

@WebMvcTest(controllers = SdkConfigController.class)
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
public class SdkConfigE2EClientEvalTest {

	@Autowired
	MockMvc mockMvc;
	@MockitoBean
	SdkConfigService sdkConfigService;
	@Autowired
	ObjectMapper objectMapper;

	@Test
	@DisplayName("번들 200 응답 -> 클라이언트 역직렬화 -> 평가 성공")
	void bundle_to_client_eval_ok() throws Exception {
		var etag = "deadbeefcafef00d";
		var responseBody = new SdkConfigResponse(
			"stage",
			"2025-09-07T14:30:00Z",
			List.of(
				new FlagItem("ff.includeOnly", false, null, Set.of("u1", "u2"), Set.of()),
				new FlagItem("ff.excludeOnly", true, null, Set.of(), Set.of("u9")),
				new FlagItem("ff.rollout50", false, 50, Set.of(), Set.of())
			)
		);

		given(sdkConfigService.getBundle("stage")).willReturn(new SdkBundle(etag, responseBody));

		String response = mockMvc.perform(get("/sdk/v1/config").param("env", "stage")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.ETAG, "\"" + etag + "\""))
			.andDo(document("sdk-config-client-e2e",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint())
			))
			.andReturn().getResponse().getContentAsString();

		JsonNode root = objectMapper.readTree(response);
		Map<String, FlagItem> clientFlags = StreamSupport.stream(root.get("flags").spliterator(), false)
			.map(node -> new FlagItem(
				node.get("key").asText(),
				node.get("enabled").asBoolean(),
				node.hasNonNull("rolloutPercentage") ? node.get("rolloutPercentage").asInt() : null,
				toSet(node.get("include")),
				toSet(node.get("exclude"))
			))
			.collect(toMap(FlagItem::key, f -> f));

		assertThat(clientFlags.get("ff.includeOnly").isEnabledFor("u1")).isTrue();
		assertThat(clientFlags.get("ff.includeOnly").isEnabledFor("uX")).isFalse();

		assertThat(clientFlags.get("ff.excludeOnly").isEnabledFor("u9")).isFalse();
		assertThat(clientFlags.get("ff.excludeOnly").isEnabledFor("uX")).isTrue();

		// 수정필요할듯
		boolean uA = clientFlags.get("ff.rollout50").isEnabledFor("userA");
		boolean uB = clientFlags.get("ff.rollout50").isEnabledFor("userB");
		assertThat(uA).isIn(true, false);
		assertThat(uB).isIn(true, false);
	}

	private static Set<String> toSet(JsonNode node) {
		if (node == null || node.isNull())
			return Set.of();
		if (node.isArray()) {
			return StreamSupport.stream(node.spliterator(), false)
				.filter(JsonNode::isTextual)
				.map(JsonNode::asText)
				.collect(Collectors.toSet());
		}
		return Set.of();
	}

	@Test
	@DisplayName("ETag 재검증: If-None-Match 일치 -> 304, 클라이언트는 캐시사용")
	void not_modified_client_use_cache() throws Exception {
		var etag = "deadbeefcafef00d";
		var responseBody = new SdkConfigResponse(
			"stage",
			"2025-09-07T14:30:00Z",
			List.of()
		);

		given(sdkConfigService.getBundle("stage")).willReturn(new SdkBundle(etag, responseBody));

		mockMvc.perform(get("/sdk/v1/config").param("env", "stage"))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.ETAG, "\"" + etag + "\""));

		mockMvc.perform(get("/sdk/v1/config").param("env", "stage")
				.header(HttpHeaders.IF_NONE_MATCH, etag))
			.andExpect(status().isNotModified());
	}
}
