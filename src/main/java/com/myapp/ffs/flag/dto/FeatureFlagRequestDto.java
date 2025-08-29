package com.myapp.ffs.flag.dto;

public record FeatureFlagRequestDto(
	String flagKey,
	String env,
	String description,
	boolean enabled
) {}
