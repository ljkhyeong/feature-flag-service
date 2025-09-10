package com.myapp.ffs.sdk.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

public class RolloutEvaluator {

	public static boolean isEnabledForUser(String userId, boolean baseEnabled, Integer rolloutPercentage,
		Set<String> include, Set<String> exclude) {
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
			return userId.hashCode() % 100;
		}
	}
}
