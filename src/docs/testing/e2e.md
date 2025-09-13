# Feature-Flag E2E 시나리오

## 목적
- 캐시 미스/히트, 404 에러, (추후) 업데이트 후 캐시 무효화까지 **엔드투엔드 흐름**을 보장한다.

## 환경
- Spring Boot
- Testcontainers: MariaDB, Redis
- Redis 캐시: `@Cacheable(value = "flags", key = "#key + ':' + #env")`

## 실행
```bash
./gradlew clean test        # E2E 포함 테스트
./gradlew asciidoctor       # REST Docs HTML 생성(선택)
```
## 시나리오
1. 캐시 미스 → DB 조회 → 200 + Redis 적재
2. 캐시 히트 → 200 (DB 미접근 간접 검증)
3. 미존재 키 → 404 + 에러 스키마(`code=FLAG_NOT_FOUND`)
4. (TODO) 업데이트 → 캐시 무효화/재적재 검증

## 2025-08-31 실행 결과 
- `redisTemplate.hasKey("new-checkout:prod")` → true (첫 요청 후 캐시 적재 확인)
- 500 오류 발생 → 원인: Jackson2JsonRedisSerializer\<Object\> 사용 시 LinkedHashMap 복원
- 해결: GenericJackson2JsonRedisSerializer로 교체 후 정상 동작
  - ✅ 캐시 미스: DB 조회 후 200 OK + Redis 적재 확인
  - ✅ 캐시 히트: 두 번째 요청 시 캐시에서 바로 응답 확인
  - ✅ 404: 존재하지 않는 flag 요청 시 FLAG_NOT_FOUND 에러 스키마 반환
  - ⏳ 업데이트 API 없음: 캐시 무효화/재적재 시나리오는 TODO 상태

## 2025-09-01 업데이트
- CacheConfig에 프리픽스(`ffs:{cacheName}::`), TTL(30분), null 캐싱 금지 반영
- 표준 에러 스키마 `{code, message, path, timestamp}` 확정

## 2025-09-03 업데이트

### 시나리오 확장
4. 업데이트 후 캐시 무효화/재적재 검증
  - 최초 조회로 캐시 적재 확인
  - `PUT /api/flags/{id}`로 env 변경 → 캐시 키 제거 확인
  - 재조회 시 최신 enabled 값 반영

5. 토글 후 캐시 무효화/재적재 검증
  - 최초 조회로 캐시 적재
  - `PATCH /api/flags/{id}/toggle` 수행 → 캐시 키 제거 확인
  - 재조회 시 토글 결과 반영 확인

### 실행 로그 요약
- `EXISTS flags::<key>:<env>` 또는 `EXISTS ffs:flags::<key>:<env>`로 캐시 적재/삭제 확인
- 업데이트/토글 이후 `TTL`은 신규 적재 시점부터 다시 계산됨

## 2025-09-04 업데이트

### 추가 시나리오/문서화
- `PATCH /api/flags/{id}/toggle` 성공 케이스 문서화 완료
    - 최초 조회 → 캐시 적재 → 토글 → 캐시 무효화 확인 → 재조회 시 토글 결과 반영

## 2025-09-09 업데이트
- `/sdk/v1/config` 엔드포인트 초안 구현
  - ETag 헤더 지원, If-None-Match 처리 → 304 응답
  - gzip 응답 활성화
- REST Docs: 200 / 304 케이스 문서화 완료
- ETag는 payload 해시 기반 → rulesJson 변경 시 자동 변경됨 << 테스트 필요
- redis-cli 확인 결과, 캐시된 SDK 번들 키는 rulesJson이 바뀌면 자동 invalidation << 테스트 필요


## 2025-09-10 업데이트
- rolloutPercentage, include, exclude 필드 SDK 응답에 추가
- `rulesJson`(JSON) 필드 파싱 로직 적용
  - `{ "include": ["u1"], "exclude": ["u9"] }` 구조
  - fallback: 문자열 `"u1,u2"`도 허용
- REST Docs: flags[].rolloutPercentage/include/exclude 문서화 완료
- 운영 팁: rulesJson 필드는 `{ include: [...], exclude: [...] }` 구조 권장.
  문자열 한 줄("u1,u2")도 수용하지만, 가독성과 안정성 때문에 배열 사용을 기본으로.

## 2025-09-11 업데이트
- k6 스크립트 초안 작성 (`scripts/k6/sdk_smoke.js`)
- 시나리오
  1. `/sdk/v1/config?env=stage` 최초 조회 (200 + ETag)
  2. 동일 ETag로 재요청 (304)
  3. rulesJson 수정 후 다시 조회 (ETag 변경, 200)
- VU = 5, Duration = 10s

## 2025-09-13 

### 통합 테스트
- `SdkConfigE2EClientEvalTest`
  - `/sdk/v1/config` 200 응답 → 클라이언트 모델(FlagItem)로 역직렬화
  - include/exclude/rollout 우선순위 검증
  - ETag 304 재검증 테스트 추가

### 확인 결과
- 서버 스키마 변경 없이 클라이언트 평가기가 정상 동작
- If-None-Match 일치 시 304 → 클라이언트 캐시 사용 시나리오 유효