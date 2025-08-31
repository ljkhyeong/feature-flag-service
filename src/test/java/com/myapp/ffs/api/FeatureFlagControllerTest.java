package com.myapp.ffs.api;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.request.RequestDocumentation;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.myapp.ffs.flag.domain.FeatureFlag;
import com.myapp.ffs.flag.dto.FeatureFlagRequestDto;
import com.myapp.ffs.flag.dto.FeatureFlagResponseDto;
import com.myapp.ffs.flag.service.FeatureFlagService;

@WebMvcTest(controllers = FeatureFlagController.class)
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
public class FeatureFlagControllerTest {
	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	FeatureFlagService featureFlagService;

	@Test
	@DisplayName("GET /api/flags/{env}/{key} - 200 성공")
	void getFlag_success()	throws Exception {
		// given
		String key = "checkout.newPayment";
		String env = "stage";
		given(featureFlagService.find(key, env))
			.willReturn(new FeatureFlagResponseDto(1L, key, env, true));

		// when
		// then
		mockMvc.perform(get("/api/flags/{env}/{key}", env, key)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.flagKey").value(key))
			.andExpect(jsonPath("$.env").value(env))
			.andDo(document("get-flag-success",
				pathParameters(
					parameterWithName("env").description("환경 (e.g prod/stage)"),
					parameterWithName("key").description("플래그 ")
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
	@DisplayName("GET /api/flags/{env}/{key} - 200 성공")
	void getFlag_fail() throws Exception {
		// given
		String key = "unknown";
		String env = "stage";
		given(featureFlagService.find(key, env))
			.willThrow(new NoSuchElementException("flag not found"));

		// when
		// then
		mockMvc.perform(get("/api/flags/{env}/{key}", env, key)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andDo(document("get-flag-404",
				pathParameters(
					parameterWithName("env").description("환경 (예: prod, stage)"),
					parameterWithName("key").description("플래그 키")
				),
				responseFields(
					fieldWithPath("code").description("에러 코드 (예: FLAG_NOT_FOUND)"),
					fieldWithPath("message").description("에러 메시지"),
					fieldWithPath("detail").description("에러 상세")
					)
			));
	}


	@Test
	void createFlag() throws Exception {
		String json = """
            { "flagKey":"checkout.newPayment", "env":"stage", "enabled":true }
            """;

		given(featureFlagService.create(any()))
			.willReturn(new FeatureFlagResponseDto(1L,"checkout.newPayment", "stage", true));

		mockMvc.perform(post("/api/flags")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(document("create-flag",
				responseFields(
					fieldWithPath("id").description("플래그 ID").optional(),
					fieldWithPath("flagKey").description("플래그 키"),
					fieldWithPath("env").description("환경"),
					fieldWithPath("enabled").description("활성화 여부")
				)));
	}

	@Test
	void updateFlag() throws Exception {
		String json = """
            { "flagKey":"checkout.newPayment", "env":"prod", "enabled":true }
            """;

		given(featureFlagService.update(anyLong(), any()))
			.willReturn(new FeatureFlagResponseDto(1L, "checkout.newPayment", "prod", true));

		mockMvc.perform(put("/api/flags/{id}", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(document("update-flag",
				pathParameters(
					parameterWithName("id").description("플래그 ID")
				)
			));
	}

	@Test
	void deleteFlag() throws Exception {
		mockMvc.perform(delete("/api/flags/{id}", 1L))
			.andExpect(status().isNoContent())
			.andDo(document("delete-flag",
				pathParameters(
					parameterWithName("id").description("플래그 ID")
				)
			));
	}

}
