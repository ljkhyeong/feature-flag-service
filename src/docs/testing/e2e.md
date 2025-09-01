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
- 500 오류 발생 → 원인: Jackson2JsonRedisSerializer<Object> 사용 시 LinkedHashMap 복원
- 해결: GenericJackson2JsonRedisSerializer로 교체 후 정상 동작
  - ✅ 캐시 미스: DB 조회 후 200 OK + Redis 적재 확인
  - ✅ 캐시 히트: 두 번째 요청 시 캐시에서 바로 응답 확인
  - ✅ 404: 존재하지 않는 flag 요청 시 FLAG_NOT_FOUND 에러 스키마 반환
  - ⏳ 업데이트 API 없음: 캐시 무효화/재적재 시나리오는 TODO 상태
