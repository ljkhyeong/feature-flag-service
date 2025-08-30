package com.myapp.ffs.api;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
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
			.andDo(document("create-flag"));
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
			.andDo(document("update-flag"));
	}

	@Test
	void deleteFlag() throws Exception {
		mockMvc.perform(delete("/api/flags/{id}", 1L))
			.andExpect(status().isNoContent())
			.andDo(document("delete-flag"));
	}

}
