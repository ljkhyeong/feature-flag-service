package com.myapp.ffs.sdk.client;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RolloutEvaluator {

	public static boolean isEnabledForUser(String userId, boolean baseEnabled, Integer rolloutPercentage,
		Set<String> include, Set<String> exclude) {

		if (userId == null)
			return baseEnabled;
		if (exclude != null && exclude.contains(userId)) {
			return false;
		}
		if (include != null && include.contains(userId)) {
			return true;
		}
		if (rolloutPercentage != null) {
			int bucket = bucket(userId);
			return bucket < rolloutPercentage;
		}

		return baseEnabled;
	}

	private static int bucket(String userId) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			byte[] hash = messageDigest.digest(userId.getBytes(StandardCharsets.UTF_8));
			int val = ((hash[0] & 0xff) << 8) | (hash[1] & 0xff);
			return val % 100;
		} catch (NoSuchAlgorithmException e) {
			return Math.abs(userId.hashCode() % 100);
		}
	}
}
