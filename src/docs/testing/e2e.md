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
## 시나리오
1. 캐시 미스 → DB 조회 → 200 + Redis 적재
2. 캐시 히트 → 200 (DB 미접근 간접 검증)
3. 미존재 키 → 404 + 에러 스키마(`code=FLAG_NOT_FOUND`)
4. (TODO) 업데이트 → 캐시 무효화/재적재 검증
