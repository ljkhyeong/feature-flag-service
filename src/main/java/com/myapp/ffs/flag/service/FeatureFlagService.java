package com.myapp.ffs.flag.service;

import java.util.NoSuchElementException;
import java.util.Objects;

import org.apache.logging.log4j.util.PropertySource;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.ffs.exception.ApplicationException;
import com.myapp.ffs.exception.ErrorCode;
import com.myapp.ffs.flag.domain.FeatureFlag;
import com.myapp.ffs.flag.dto.FeatureFlagRequestDto;
import com.myapp.ffs.flag.dto.FeatureFlagResponseDto;
import com.myapp.ffs.flag.repository.FeatureFlagRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureFlagService {
	private final FeatureFlagRepository featureFlagRepository;
	private final CacheManager cacheManager;
	private final Environment environment;
	// TODO : 로깅 AOP 사용하자
	private static final String CACHE_FLAG = "flags";

	@Transactional
	public FeatureFlagResponseDto create(FeatureFlagRequestDto dto) {
		FeatureFlag flag = FeatureFlag.builder()
			.flagKey(dto.flagKey())
			.env(dto.env())
			.description(dto.description())
			.enabled(dto.enabled())
			.build();

		FeatureFlag savedFlag = featureFlagRepository.save(flag);

		return FeatureFlagResponseDto.from(savedFlag);
	}

	@Cacheable(value = CACHE_FLAG, key = "#key + ':' + #env")
	@Transactional(readOnly = true)
	public FeatureFlagResponseDto find(String key, String env) {
		FeatureFlagResponseDto dto = featureFlagRepository.findByFlagKeyAndEnv(key, env)
			.map(FeatureFlagResponseDto::from)
			.orElseThrow(() -> new ApplicationException(ErrorCode.FLAG_NOT_FOUND));

		if (isDevOrTest()) {
			log.info("[CACHE:MISS] flags {}:{}", key, env);
		}
		return dto;
	}

	@CachePut(value = CACHE_FLAG, key = "#result.flagKey() + ':' + #result.env()")
	@Transactional
	public FeatureFlagResponseDto update(Long id, FeatureFlagRequestDto dto) {
		FeatureFlag flag = featureFlagRepository.findById(id)
			.orElseThrow(() -> new ApplicationException(ErrorCode.FLAG_NOT_FOUND));

		String oldKey = flag.getFlagKey();
		String oldEnv = flag.getEnv();

		flag.change(dto.flagKey(), dto.env(), dto.description(), dto.enabled());
		if (!Objects.equals(oldKey, flag.getFlagKey()) || !Objects.equals(oldEnv, flag.getEnv())) {
			evictFlags(oldKey, oldEnv);
		}

		if (isDevOrTest()) {
			log.info("[CACHE:PUT] flags {}:{} (update)", flag.getFlagKey(), flag.getEnv());
		}

		return FeatureFlagResponseDto.from(flag);
	}

	@CachePut(value = CACHE_FLAG, key = "#result.flagKey() + ':' + #result.env()")
	@Transactional
	public FeatureFlagResponseDto toggle(Long id) {
		FeatureFlag flag = featureFlagRepository.findById(id)
			.orElseThrow(() -> new ApplicationException(ErrorCode.FLAG_NOT_FOUND));

		flag.toggle();

		if (isDevOrTest()) {
			log.info("[CACHE:PUT] flags {}:{} (toggle -> enabled={}", flag.getFlagKey(), flag.getEnv(),
				flag.isEnabled());
		}
		return FeatureFlagResponseDto.from(flag);
	}

	@Transactional
	public void delete(Long id) {
		FeatureFlag flag = featureFlagRepository.findById(id)
			.orElseThrow(() -> new ApplicationException(ErrorCode.FLAG_NOT_FOUND));

		featureFlagRepository.delete(flag);
		evictFlags(flag.getFlagKey(), flag.getEnv());
		if (isDevOrTest()) {
			log.info("[CACHE:EVICT] flags {}:{} (delete)", flag.getFlagKey(), flag.getEnv());
		}
	}

	private void evictFlags(String key, String env) {
		Cache cache = cacheManager.getCache(CACHE_FLAG);
		if (cache != null && key != null && env != null) {
			cache.evict(key + ":" + env);
		}
	}

	private boolean isDevOrTest() {
		for (String p : environment.getActiveProfiles()) {
			if (Objects.equals(p.toLowerCase(), "dev") || Objects.equals(p.toLowerCase(), "test")) {
				return true;
			}
		}
		return false;
	}


}
