package com.myapp.ffs.flag.service;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.myapp.ffs.flag.domain.FeatureFlag;
import com.myapp.ffs.flag.dto.FeatureFlagRequestDto;
import com.myapp.ffs.flag.dto.FeatureFlagResponseDto;
import com.myapp.ffs.flag.repository.FeatureFlagRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeatureFlagService {
	private final FeatureFlagRepository featureFlagRepository;

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

	@Transactional(readOnly = true)
	public FeatureFlagResponseDto find(String key, String env) {
		return featureFlagRepository.findByFlagKeyAndEnv(key, env)
			.map(FeatureFlagResponseDto::from)
			.orElseThrow(() -> new NoSuchElementException("flag not found"));
	}

	@Transactional
	public FeatureFlagResponseDto update(Long id, FeatureFlagRequestDto dto) {
		FeatureFlag flag = featureFlagRepository.findById(id)
			.orElseThrow(() -> new NoSuchElementException("flag not found"));

		flag.change(dto.flagKey(), dto.env(), dto.description(), dto.enabled());
		return FeatureFlagResponseDto.from(flag);
	}

	@Transactional
	public void delete(Long id) {
		if (!featureFlagRepository.existsById(id))
			throw new NoSuchElementException("flag not found");
		featureFlagRepository.deleteById(id);
	}



}
