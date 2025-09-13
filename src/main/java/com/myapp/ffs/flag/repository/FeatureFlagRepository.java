package com.myapp.ffs.flag.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myapp.ffs.flag.domain.FeatureFlag;

public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {
	Optional<FeatureFlag> findByFlagKeyAndEnv(String flagKey, String env);

	List<FeatureFlag> findAllByEnvOrderByFlagKey(String env);
}
