package com.myapp.ffs.e2e;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.ffs.flag.domain.FeatureFlag;
import com.myapp.ffs.flag.repository.FeatureFlagRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FeatureFlagE2ETest {

	@Container
	static MariaDBContainer<?> maria = new MariaDBContainer<>("mariadb:11.3")
		.withDatabaseName("flags")
		.withUsername("app")
		.withPassword("app");

	@Container
	static GenericContainer<?> redis = new GenericContainer<>("redis:7.2")
		.withExposedPorts(6379);

	@DynamicPropertySource
	static void props(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", () -> maria.getJdbcUrl());
		registry.add("spring.datasource.username", () -> maria.getUsername());
		registry.add("spring.datasource.password", () -> maria.getPassword());

		registry.add("spring.data.redis.host", () -> redis.getHost());
		registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
	}

	@Autowired
	MockMvc mockMvc;
	@Autowired
	FeatureFlagRepository featureFlagRepository;
	@Autowired
	StringRedisTemplate redisTemplate;
	@Autowired
	ObjectMapper objectMapper;

	final String key = "new-checkout";
	final String env = "prod";
	final String description = "desc.";
	final String cacheKey = "ffs:flags::" + key + ":" + env;
	FeatureFlag flag = FeatureFlag.builder()
		.flagKey(key)
		.env(env)
		.description(description)
		.enabled(true)
		.build();

	@BeforeEach
	void setUp() {
		redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
		featureFlagRepository.deleteAll();
		featureFlagRepository.save(flag);
	}

	@Test
	@Order(1)
	@DisplayName("캐시 미스 : 첫 요청")
	void getFlag_cacheMiss() throws Exception {
		// given
		assertThat(redisTemplate.hasKey(cacheKey)).isFalse();

		// when
		String responseJson = mockMvc.perform(get("/api/flags/{env}/{key}", env, key)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.enabled").value(true))
			.andReturn().getResponse().getContentAsString();

		// then
		JsonNode jsonNode = objectMapper.readTree(responseJson);

		assertThat(jsonNode.get("env").asText()).isEqualTo(env);
		assertThat(jsonNode.get("flagKey").asText()).isEqualTo(key);
		assertThat(redisTemplate.hasKey(cacheKey)).isTrue();
	}

	@Test
	@Order(2)
	@DisplayName("캐시 히트 : 캐시 적재 후 ")
	void getFlag_cacheHit() throws Exception {
		// given
		mockMvc.perform(get("/api/flags/{env}/{key}", env, key))
			.andExpect(status().isOk());
		assertThat(redisTemplate.hasKey(cacheKey)).isTrue();

		// when
		// then
		mockMvc.perform(get("/api/flags/{env}/{key}", env, key)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.flagKey").value(key))
			.andExpect(jsonPath("$.env").value(env));
	}

	@Test
	@Order(3)
	@DisplayName("미존재 키 : 404 + 에러")
	void getFlag_notFound() throws Exception {
		mockMvc.perform(get("/api/flags/{env}/{key}", "stage", "unknown")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("FLAG_NOT_FOUND"))
			.andExpect(jsonPath("$.message").exists());
	}

	//TODO 업데이트 후 캐시 날리는거
	@Test
	@Order(4)
	@DisplayName("업데이트 후 캐시 flush 및 새 캐시 put")
	void updateFlag_cacheEvict() throws Exception {
		// given
		mockMvc.perform(get("/api/flags/{env}/{key}", env, key))
			.andExpect(status().isOk());
		assertThat(redisTemplate.hasKey(cacheKey)).isTrue();

		// when
		String body = """
			{ "flagKey":"checkout.newPayment", "env":"stage", "enabled":true }
			""";
		mockMvc.perform(put("/api/flags/{id}",
				featureFlagRepository.findByFlagKeyAndEnv(key, env).orElseThrow().getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk());

		// then
		assertThat(redisTemplate.hasKey(cacheKey)).isFalse();
		assertThat(redisTemplate.hasKey("ffs:flags::checkout.newPayment:stage")).isTrue();
	}

	@Test
	@Order(5)
	@DisplayName("토글 후 캐시 flush, 플래그 비활성화")
	void toggleFlag_cacheEvict() throws Exception{
		// given
		mockMvc.perform(get("/api/flags/{env}/{key}", env, key))
			.andExpect(status().isOk());
		assertThat(redisTemplate.hasKey(cacheKey)).isTrue();

		// when
		mockMvc.perform(patch("/api/flags/{id}",
				featureFlagRepository.findByFlagKeyAndEnv(key, env).orElseThrow().getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.enabled").value(false));

		// then
		assertThat(redisTemplate.hasKey(cacheKey)).isFalse();
	}
}
