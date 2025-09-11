package com.myapp.ffs.sdk.service;

import static java.util.stream.Collectors.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.ffs.flag.domain.FeatureFlag;
import com.myapp.ffs.flag.repository.FeatureFlagRepository;
import com.myapp.ffs.flag.dto.FlagItem;
import com.myapp.ffs.sdk.dto.SdkBundle;
import com.myapp.ffs.sdk.dto.SdkConfigResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SdkConfigService {

	private final FeatureFlagRepository featureFlagRepository;
	private final ObjectMapper objectMapper;

	public SdkBundle getBundle(String env) {
		List<FeatureFlag> flags = featureFlagRepository.findAllByEnv(env);

		List<FlagItem> items = flags.stream()
			.map(FlagItem::from)
			.collect(toList());

		Instant version = flags.stream()
			.map(flag -> flag.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant())
			.filter(Objects::nonNull)
			.max(Comparator.naturalOrder())
			.orElseGet(Instant::now);

		SdkConfigResponse body = new SdkConfigResponse(env, version.toString(), items);

		String etag = strongEtagOf(body);

		return new SdkBundle(etag, body);
	}

	private String strongEtagOf(Object payload) {
		try {
			byte[] json = objectMapper.writeValueAsBytes(payload);
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			byte[] digest = messageDigest.digest(json);
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < 8; i++) { // 8바이트
				stringBuilder.append(String.format("%02x", digest[i]));
			}
			return stringBuilder.toString();
		} catch (JsonProcessingException | NoSuchAlgorithmException e) {
			return Long.toHexString(System.currentTimeMillis());
		}
	}


}
