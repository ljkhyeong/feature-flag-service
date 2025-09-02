# ADR-001: Redis 캐시 직렬화 정책 및 키 규칙

## 상태
Accepted -> **Applied (2025-09-01)**

## 배경
- `@Cacheable(value = "flags", key = "#key + ':' + #env")`로 Feature-Flag 읽기 경로를 캐시한다.
- 기존 설정은 `Jackson2JsonRedisSerializer<>(Object.class)`를 사용하여 **타입정보 미포함**.
- E2E에서 캐시 히트 시 JSON → `LinkedHashMap` 복원으로 인해 서비스 반환 타입(`FeatureFlagResponseDto`)과 불일치가 발생, 500 오류(ClassCastException) 유발 가능.

## 결정
1. **값 직렬화기 변경**
   - 기본값: `GenericJackson2JsonRedisSerializer` 사용 (타입정보 포함 → DTO 복원 보장).
   - 대안: “flags” 캐시에 한해 `Jackson2JsonRedisSerializer<FeatureFlagResponseDto>` 적용.
2. **키 규칙 고정**
   - `{key}:{env}` (예: `new-checkout:prod`)
   - 서비스/테스트/운영 로그에서 동일 규칙 사용.
3. **갱신 정책**
   - 업데이트/토글 시 `@CacheEvict(value = "flags", key = "#key + ':' + #env")`
   - 대량 변경 시 `allEntries = true` 고려.
4. **운영 설정(추가)**
   - **키 프리픽스**: `ffs:{cacheName}::` (예: `ffs:flags::checkout.newPayment:stage`)
   - **TTL**: 기본 30분(필요 시 조정)
   - **null 캐싱 금지**: `disableCachingNullValues()`
5. **에러 스키마(추가)**
   - 표준 에러 응답: `{ code, message, path, timestamp(UTC, ISO-8601) }`

[//]: # (4. **가시성**)

[//]: # (   - 히트/미스 로깅을 &#40;개발/테스트 프로파일&#41;에서만 활성화.)

[//]: # (   - Micrometer로 DB 쿼리 카운트/레이턴시&#40;p95&#41; 추적 예정.)

## 근거
- 타입 안정성: 캐시 히트 시 DTO 복원 실패로 인한 런타임 오류 제거.
- 유지보수성: 테스트/운영 환경 간 동작 일관성 확보.
- 보안/노출: 타입정보가 JSON에 포함되는 점은 내부 캐시로 한정되므로 수용 가능.

## 결과
- E2E 시나리오 1~3 안정 통과.
- 업데이트 시나리오(캐시 무효화/재적재)는 API 구현 후 테스트에 포함 예정.

## 변경 내역 
- 2025-08-31 : GenericJackson2JsonRedisSerializer로 교체 완료 
- 2025-09-01: 프리픽스/TTL/null 캐싱 금지 적용, 표준 에러 스키마 확정