package com.myapp.ffs.sdk;

import static org.assertj.core.api.Assertions.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.myapp.ffs.sdk.dto.SdkBundle;
import com.myapp.ffs.sdk.service.SdkConfigService;

@SpringBootTest
@Disabled("CI 안정화까지 비활성화 or 단위테스트화")
class SdkBundleDeterminismTest {

	@Autowired
	SdkConfigService sdkConfigService;

	@Test
	@DisplayName("동일 데이터/환경은 Payload와 Etag가 같음")
	void deterministic() {
		String env = "stage";
		SdkBundle b1 = sdkConfigService.getBundle(env);
		SdkBundle b2 = sdkConfigService.getBundle(env);

		assertThat(b1.etag()).isNotBlank();
		assertThat(b1.etag()).isEqualTo(b2.etag());
		assertThat(b1.payload()).usingRecursiveComparison().isEqualTo(b2.payload());
	}
}
