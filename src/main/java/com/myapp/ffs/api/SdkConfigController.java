package com.myapp.ffs.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myapp.ffs.sdk.SdkConfigService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sdk/v1")
@RequiredArgsConstructor
public class SdkConfigController {

	private final SdkConfigService sdkConfigService;

	@GetMapping("/config")
	public ResponseEntity<Sdk>
}
