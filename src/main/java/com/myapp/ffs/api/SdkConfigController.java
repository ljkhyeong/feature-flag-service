package com.myapp.ffs.api;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.myapp.ffs.sdk.service.SdkConfigService;
import com.myapp.ffs.sdk.dto.SdkBundle;
import com.myapp.ffs.sdk.dto.SdkConfigResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sdk/v1")
@RequiredArgsConstructor
public class SdkConfigController {

	private final SdkConfigService sdkConfigService;

	@GetMapping("/config")
	public ResponseEntity<SdkConfigResponse> getConfig(@RequestParam String env,
		@RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
		SdkBundle bundle = sdkConfigService.getBundle(env);

		if (ifNoneMatch != null && ifNoneMatch.equals(bundle.etag())) {
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
				.eTag(bundle.etag()) // 응답 헤더 키는 'Etag'로 표시됨 (HTTP는 대소문자 비구분). k6 등에서는 case-insensitive로 읽을 것.
				.cacheControl(CacheControl.noCache()) // 일부 캐시/프록시 서버에서 304응답 헤더를 덮어씌우는 경우도 있다고 해서 추가함
				.build();
		}

		return ResponseEntity.ok()
			.eTag(bundle.etag())
			.cacheControl(CacheControl.noCache())
			.body(bundle.payload());
	}

}
