package com.myapp.ffs.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.myapp.ffs.flag.domain.FeatureFlag;
import com.myapp.ffs.flag.repository.FeatureFlagRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class FeatureFlagRepositoryTest {

	@Container
	static MariaDBContainer<?> db = new MariaDBContainer<>("mariadb:11")
		.withDatabaseName("flags")
		.withUsername("app")
		.withPassword("app");

	@DynamicPropertySource
	static void configure(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", db::getJdbcUrl);
		registry.add("spring.datasource.username", db::getUsername);
		registry.add("spring.datasource.password", db::getPassword);
	}

	@Autowired
	FeatureFlagRepository repository;

	@Test
	void saveAndFind() {
		FeatureFlag flag = FeatureFlag.builder()
			.flagKey("checkout.newPayment")
			.env("stage")
			.enabled(true)
			.build();

		repository.save(flag);

		Optional<FeatureFlag> found = repository.findByFlagKeyAndEnv("checkout.newPayment", "stage");
		assertThat(found).isPresent();
		assertThat(found.get().isEnabled()).isTrue();
	}
}
