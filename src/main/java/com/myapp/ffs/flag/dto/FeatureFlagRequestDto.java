package com.myapp.ffs.flag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeatureFlagRequestDto(
	@NotBlank
	String flagKey,
	@NotBlank
	String env,
	String description,
	@NotNull
	boolean enabled
) {}
