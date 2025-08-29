package com.myapp.ffs.flag.dto;

import com.myapp.ffs.flag.domain.FeatureFlag;

public record FeatureFlagResponseDto (
	Long id,
	String flagKey,
	String env,
	boolean enabled
) {
	public static FeatureFlagResponseDto from(FeatureFlag entity) {
		return new FeatureFlagResponseDto(
			entity.getId(), entity.getFlagKey(), entity.getEnv(), entity.isEnabled()
		);
	}
}
