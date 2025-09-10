package com.myapp.ffs.api;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.myapp.ffs.sdk.SdkConfigService;
import com.myapp.ffs.flag.dto.FlagItem;
import com.myapp.ffs.sdk.dto.SdkBundle;
import com.myapp.ffs.sdk.dto.SdkConfigResponse;

@WebMvcTest(controllers = SdkConfigController.class)
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
public class SdkConfigControllerTest {

	@Autowired
	MockMvc mockMvc;
	@MockitoBean
	SdkConfigService sdkConfigService;

	String env = "stage";
	String version = "2025-09-06T10:30:00Z";
	String etag = "abc123deadbeef00";
	String flagKey1 = "checkout.newPayment";
	String flagKey2 = "exp.searchV2";
	SdkConfigResponse body = new SdkConfigResponse(
		env,
		version,
		List.of(new FlagItem(flagKey1, true, 100, Set.of(), Set.of()),
			new FlagItem(flagKey2, false, 100, Set.of(), Set.of()))
	);
	@Test
	@DisplayName("GET /sdk/v1/config?env=stage - 200 + ETag")
	void getConfig_ok() throws Exception {
		given(sdkConfigService.getBundle(env)).willReturn(new SdkBundle(etag, body));

		mockMvc.perform(get("/sdk/v1/config").param("env", "stage")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.ETAG, "\"" + etag + "\""))
			.andExpect(jsonPath("$.env").value(env))
			.andExpect(jsonPath("$.flags[0].key").value(flagKey1))
			.andDo(document("get-sdk-config-200",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				queryParameters(
					parameterWithName("env").description("환경")
				),
				responseFields(
					fieldWithPath("env").description("환경"),
					fieldWithPath("version").description("번들 버전(최신 업데이트 시간)"),
					fieldWithPath("flags[].key").description("플래그 키"),
					fieldWithPath("flags[].enabled").description("활성화 여부"),
					fieldWithPath("flags[].rolloutPercentage").description("롤아웃 퍼센티지").optional(),
					fieldWithPath("flags[].include[]").description("무조건 포함할 사용자 ID").optional(),
					fieldWithPath("flags[].exclude[]").description("무조건 제외할 사용자 ID").optional()
				)
			));
	}

	@Test
	@DisplayName("GET /sdk/v1/config If-None-Match 일치 → 304")
	void getConfig_notModified() throws Exception {
		given(sdkConfigService.getBundle(env)).willReturn(new SdkBundle(etag, body));

		mockMvc.perform(get("/sdk/v1/config").param("env", "stage")
				.header(HttpHeaders.IF_NONE_MATCH, etag))
			.andExpect(status().isNotModified())
			.andExpect(header().string(HttpHeaders.ETAG, "\"" + etag + "\""))
			.andDo(document("get-sdk-config-304",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				queryParameters(
					parameterWithName("env").description("환경")
				)
			));
	}
}
