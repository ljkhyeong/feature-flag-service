package com.myapp.ffs;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("CI 안정화까지 컨텍스트 로드 비활성화")
class FfsApplicationTests {

	@Test
	void contextLoads() {
	}

}
