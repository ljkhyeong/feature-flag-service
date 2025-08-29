package com.myapp.ffs.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myapp.ffs.flag.dto.FeatureFlagRequestDto;
import com.myapp.ffs.flag.dto.FeatureFlagResponseDto;
import com.myapp.ffs.flag.service.FeatureFlagService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/flags")
public class FeatureFlagController {
	private final FeatureFlagService featureFlagService;

	@PostMapping
	public ResponseEntity<FeatureFlagResponseDto> create(@RequestBody @Valid FeatureFlagRequestDto dto) {
		return ResponseEntity.ok(featureFlagService.create(dto));
	}

	@GetMapping("/{env}/{key}")
	public ResponseEntity<FeatureFlagResponseDto> find(@PathVariable String env, @PathVariable String key) {
		return ResponseEntity.ok(featureFlagService.find(key, env));
	}
}
