package com.myapp.ffs.support;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class CacheHitLoggingAspect {

	@Value("${app.cache.log-hit:false}")
	boolean logHit;

	@Pointcut("execution(* com.myapp.ffs.flag.service.FeatureFlagService.find(..))")
	void findFlagPointCut() {
	}

	@Around("findFlagPointCut() && args(key, env)")
	public Object aroundFind(ProceedingJoinPoint proceedingJoinPoint, String key, String env) throws Throwable {
		try {
			Object ret = proceedingJoinPoint.proceed();
			if (logHit) {

			}
			return ret;
		} catch (Throwable t) {
			throw t;
		}
	}
}
