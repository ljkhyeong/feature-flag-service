package com.myapp.ffs.api;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.myapp.ffs.exception.ApplicationException;
import com.myapp.ffs.exception.ErrorCode;
import com.myapp.ffs.flag.dto.FeatureFlagResponseDto;
import com.myapp.ffs.flag.service.FeatureFlagService;
import com.myapp.ffs.support.GlobalExceptionHandler;

@WebMvcTest(controllers = FeatureFlagController.class)
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
@Import(GlobalExceptionHandler.class)
class FeatureFlagControllerDocsTest {
	@Autowired
	MockMvc mockMvc;
	@MockitoBean
	FeatureFlagService featureFlagService;

	private static final String KEY = "checkout.newPayment";
	private static final String ENV = "stage";
	private static final Long ID = 1L;

	@Test
	@DisplayName("GET /api/flags/{env}/{key} - 200 성공 (플래그 조회)")
	void getFlag_success() throws Exception {
		// given
		given(featureFlagService.find(KEY, ENV))
			.willReturn(new FeatureFlagResponseDto(ID, KEY, ENV, true));

		// when
		// then
		mockMvc.perform(get("/api/flags/{env}/{key}", ENV, KEY)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.flagKey").value(KEY))
			.andExpect(jsonPath("$.env").value(ENV))
			.andDo(document("get-flag-success",
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("env").description("환경 (e.g prod/stage)"),
					parameterWithName("key").description("플래그 키")
				),
				responseFields(
					fieldWithPath("id").description("플래그 ID").optional(),
					fieldWithPath("flagKey").description("플래그 키"),
					fieldWithPath("env").description("환경"),
					fieldWithPath("enabled").description("활성화 여부")
				)
			));
	}

	@Test
	@DisplayName("GET /api/flags/{env}/{key} - 404 실패 (해당하는 key 플래그 없음)")
	void getFlag_notFound() throws Exception {
		// given
		given(featureFlagService.find("unknown", ENV))
			.willThrow(new ApplicationException(ErrorCode.FLAG_NOT_FOUND));

		// when
		// then
		mockMvc.perform(get("/api/flags/{env}/{key}", ENV, "unknown")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andDo(document("get-flag-404",
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("env").description("환경 (예: prod, stage)"),
					parameterWithName("key").description("플래그 키")
				),
				responseFields(
					fieldWithPath("code").description("에러 코드 (예: FLAG_NOT_FOUND)"),
					fieldWithPath("message").description("에러 메시지"),
					fieldWithPath("path").description("요청 경로"),
					fieldWithPath("timestamp").description("발생 시각(UTC, ISO-8601)")
				)
			));
	}

	@Test
	@DisplayName("POST /api/flags - 200 성공 (플래그 생성)")
	void createFlag() throws Exception {
		String json = """
			{ "flagKey":"checkout.newPayment", "env":"stage", "enabled":true }
			""";

		// given
		given(featureFlagService.create(any()))
			.willReturn(new FeatureFlagResponseDto(ID, KEY, ENV, true));

		// when
		// then
		mockMvc.perform(post("/api/flags")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(document("create-flag",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestFields(
					fieldWithPath("flagKey").description("플래그 키"),
					fieldWithPath("env").description("환경"),
					fieldWithPath("enabled").description("활성화 여부")
				),
				responseFields(
					fieldWithPath("id").description("플래그 ID").optional(),
					fieldWithPath("flagKey").description("플래그 키"),
					fieldWithPath("env").description("환경"),
					fieldWithPath("enabled").description("활성화 여부")
				)));
	}

	@Test
	@DisplayName("POST /api/flags - 400 실패 (필수필드 누락)")
	void createFlag_fail_missingField() throws Exception {
		String json = """
			{ "env":"stage", "enabled":true }
			""";

		// when
		// then
		mockMvc.perform(post("/api/flags")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andDo(document("create-flag-fail-400",
				preprocessResponse(prettyPrint()),
				responseFields(
					fieldWithPath("code").description("에러 코드"),
					fieldWithPath("message").description("에러 메시지"),
					fieldWithPath("path").description("요청 경로"),
					fieldWithPath("timestamp").description("발생 시각(UTC, ISO-8601)")
				)));
	}

	@Test
	@DisplayName("PUT /api/flags/{id} - 200 성공 (플래그 수정)")
	void updateFlag() throws Exception {
		String json = """
			{ "flagKey":"checkout.newPayment", "env":"prod", "enabled":true }
			""";

		given(featureFlagService.update(eq(ID), any()))
			.willReturn(new FeatureFlagResponseDto(ID, KEY, "prod", true));

		mockMvc.perform(put("/api/flags/{id}", ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(document("update-flag",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("id").description("플래그 ID")
				),
				requestFields(
					fieldWithPath("flagKey").description("플래그 키"),
					fieldWithPath("env").description("환경"),
					fieldWithPath("enabled").description("활성화 여부")
				),
				responseFields(
					fieldWithPath("id").description("플래그 ID").optional(),
					fieldWithPath("flagKey").description("플래그 키"),
					fieldWithPath("env").description("환경"),
					fieldWithPath("enabled").description("활성화 여부")
				)
			));
	}

	@Test
	@DisplayName("PUT /api/flags/{id} - 404 실패 (해당하는 key 플래그 없음)")
	void updateFlag_fail() throws Exception {
		String json = """
        { "flagKey":"checkout.newPayment", "env":"prod", "enabled":true }
        """;

		given(featureFlagService.update(eq(99L), any()))
			.willThrow(new ApplicationException(ErrorCode.FLAG_NOT_FOUND));

		mockMvc.perform(put("/api/flags/{id}", 99L)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isNotFound())
			.andDo(document("update-flag-fail-404",
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("id").description("플래그 ID")
				),
				requestFields(
					fieldWithPath("flagKey").description("플래그 키"),
					fieldWithPath("env").description("환경"),
					fieldWithPath("enabled").description("활성화 여부")
				),
				responseFields(
					fieldWithPath("code").description("에러 코드 (예: FLAG_NOT_FOUND)"),
					fieldWithPath("message").description("에러 메시지"),
					fieldWithPath("path").description("요청 경로"),
					fieldWithPath("timestamp").description("발생 시각(UTC, ISO-8601)")
				)
			));
	}

	@Test
	@DisplayName("PUT /api/flags/{id} - 400 실패 (requestBody 없음)")
	void updateFlag_fail_whenBodyMissing() throws Exception {
		mockMvc.perform(put("/api/flags/{id}", 99L)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andDo(document("update-flag-fail-400",
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("id").description("플래그 ID")
				),
				responseFields(
					fieldWithPath("code").description("에러 코드 (예: FLAG_NOT_FOUND)"),
					fieldWithPath("message").description("에러 메시지"),
					fieldWithPath("path").description("요청 경로"),
					fieldWithPath("timestamp").description("발생 시각(UTC, ISO-8601)")
				)
			));
	}

	@Test
	@DisplayName("DELETE /api/flags/{id} - 204 성공 (플래그 삭제)")
	void deleteFlag() throws Exception {
		mockMvc.perform(delete("/api/flags/{id}", ID))
			.andExpect(status().isNoContent())
			.andDo(document("delete-flag",
				pathParameters(
					parameterWithName("id").description("플래그 ID")
				)
			));
	}

	@Test
	@DisplayName("PATCH /api/flags/{id} - 200 성공 (플래그 토글)")
	void toggleFlag() throws Exception {
		given(featureFlagService.toggle(anyLong()))
			.willReturn(new FeatureFlagResponseDto(ID, KEY, ENV, false));

		mockMvc.perform(patch("/api/flags/{id}", ID)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.flagKey").value(KEY))
			.andExpect(jsonPath("$.env").value(ENV))
			.andExpect(jsonPath("$.enabled").value(false))
			.andDo(document("toggle-flag",
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("id").description("플래그 ID")
				),
				responseFields(
					fieldWithPath("id").description("플래그 ID").optional(),
					fieldWithPath("flagKey").description("플래그 키"),
					fieldWithPath("env").description("환경"),
					fieldWithPath("enabled").description("활성화 여부")
				)
			));
	}
}
