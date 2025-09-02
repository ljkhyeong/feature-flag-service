package com.myapp.ffs.config;

import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.*;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.myapp.ffs.flag.domain.FeatureFlag;
import com.myapp.ffs.flag.dto.FeatureFlagResponseDto;

@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

		RedisCacheConfiguration redisCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
			.disableCachingNullValues() // 부하 방지를 위해 키느냐, TTL동안 오염되도록 냅두느냐
			.entryTtl(Duration.ofMinutes(30))
			.computePrefixWith(cacheName -> "ffs:" + cacheName + "::")
			.serializeKeysWith(fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(fromSerializer(new GenericJackson2JsonRedisSerializer()));

		return RedisCacheManager.builder(redisConnectionFactory)
			.cacheDefaults(redisCacheConfig)
			.build();
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(factory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		return redisTemplate;
	}

}
