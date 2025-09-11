package com.myapp.ffs.flag.dto;

import java.util.Set;
import java.util.stream.Collectors;

import com.myapp.ffs.flag.domain.FeatureFlag;
import com.myapp.ffs.sdk.client.RolloutEvaluator;
import com.myapp.ffs.support.RulesJsonParser;

public record FlagItem (
	String key,
	boolean enabled,
	Integer rolloutPercentage, // 0-100 (null이면 롤아웃 미적용)
	Set<String> include,
	Set<String> exclude // 제외할 userId
){

	public static FlagItem from(FeatureFlag flag) {
		return new FlagItem(flag.getFlagKey(),flag.isEnabled(), flag.getRolloutPercentage(),
			RulesJsonParser.parseInclude(flag.getRulesJson()), RulesJsonParser.parseExclude(flag.getRulesJson()));
	}

	public boolean isEnabledFor(String userId) {
		return RolloutEvaluator.isEnabledForUser(userId, enabled, rolloutPercentage, include, exclude);
	}
}
