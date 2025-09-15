# Feature Flag Service

Spring Boot 기반의 **Feature Flag Management Service**.  
사용자 그룹별 세분화, 점진적 롤아웃, 실시간 설정값 제공을 지원.

---
## 🎯 목표
- **세그먼트 타깃팅**: 사용자 그룹별로 기능을 다르게 적용
- **퍼센트 롤아웃**: 전체 사용자 중 일부 비율에게만 새로운 기능을 배포 (점진적 테스트, 롤백 가능)
- **SDK 구성 제공**: 배포 없이 실시간으로 설정값을 서버에서 내려줌

---

## 🏗️ 아키텍처 (초안)
- **Control Plane**
    - Admin API (Spring Boot)
    - MySQL (JPA, @Version 통한 낙관적 락킹)

- **Data Plane**
    - `/sdk/v1/config` (ETag, gzip)
    - Redis 캐시
    - Pub/Sub 버전 bump

--- 
## 🛠 품질 & 운영
- Testcontainers (MariaDB, Redis)
- Spring REST Docs (+ Asciidoctor)
- Spring Boot Actuator, Micrometer
- Docker Compose (로컬 통합 실행)

---

## 🚀 MVP 기능
- 플래그/변형 CRUD
- 환경별 번들 JSON + ETag 헤더
- 감사 로그 (Audit Log)
- 퍼센트 롤아웃(rolloutPercentage)
- include/exclude 기반 타겟팅 (rulesJson 파싱)

---

## 📡 SDK 번들 (/sdk/v1/config)
- 환경별 플래그 번들을 JSON으로 제공
- 헤더
- `ETag`: payload 해시 기반 (If-None-Match → 304 Not Modified 지원)
- `Cache-Control: no-cache`
     - 응답 예시:
```json
{
  "env": "stage",
  "version": "2025-09-06T10:30:00Z",
  "flags": [
    {
     "key": "checkout.newPayment",
     "enabled": true,
     "rolloutPercentage": 50,
     "include": ["u1","u2"],
     "exclude": ["u9"]
    }
  ]
}
```

---
## 🎛 SDK 클라이언트 평가
- 클라이언트 SDK는 `/sdk/v1/config` 응답을 역직렬화하여 `FlagItem`을 사용.
- 각 플래그에 대해 `flagItem.isEnabledFor(userId)` 호출 시 최종 활성 여부를 판정.
- 평가 순서:
  - i. exclude (무조건 false)
  - ii. include (무조건 true)
  - iii. rolloutPercentage (해시 기반 퍼센트)
  - iv. baseEnabled (fallback)
- 간단 사용 예:
  ```java
  FlagItem f = ...; // /sdk/v1/config로 받은 항목
  boolean on = f.isEnabledFor(userId);
  ```
---

## 📄 API 문서
- REST Docs 기반 HTML 문서
- [생성 경로](build/docs/asciidoc/index.html)

---


## 🧪 API 성능 테스트 결과 (with K6)

- **테스트 시간**: 30초
- **동시 사용자 수 (VUs)**: 10명

| 지표                             | 캐싱 전       | 캐싱 후       |
|--------------------------------|------------|------------|
| 평균 응답 시간 (avg)                 | 27.65 ms   | 11.43 ms   |
| p90 latency                    | 46.1 ms    | 21.14 ms   |
| p95 latency                    | 62.48 ms   | 36.12 ms   |
| 총 요청 수                         | 300        | 300        |
| 성공률                            | 100%       | 100%       |

✅ 평균 응답 시간 < 200ms, p95 < 500ms, 안정적 성능 확인.

---

## ⚡ 실행 방법
```bash
./gradlew bootRun
```
✅ 테스트 & 문서화
```bash
# 테스트 (REST Docs 스니펫 생성)
./gradlew clean test

# REST Docs HTML 생성
./gradlew asciidoctor

```

---
## 🔄 CI

GitHub Actions로 테스트/문서 빌드 실행

워크플로 파일: .github/workflows/ci.yml

### Steps
1) `./gradlew clean test` (REST Docs **snippets** 생성)
2) `./gradlew asciidoctor` (REST Docs **HTML** 생성)
3) 아티팩트 업로드
   - `rest-docs-snippets`: `build/generated-snippets/**`
   - `rest-docs-html`: `build/docs/asciidoc/**`

---

## 🗄️ 서버 캐싱 정책
| 항목            | 규칙/정책                                                                  |
|---------------|------------------------------------------------------------------------|
| 키 규칙          | `{key}:{env}` (예: `checkout.newPayment:stage`)                         |
| @Cacheable 설정 | `@Cacheable(value = "flags", key = "#key + ':' + #env")`               |
| 프리픽스          | `ffs:{cacheName}::` (예: `ffs:flags::checkout.newPayment:stage`)        |
| TTL           | 30분 (운영 정책에 따라 조정)                                                     |
| null 캐싱       | 금지 (`disableCachingNullValues()`)                                      |
| 직렬화기          | `GenericJackson2JsonRedisSerializer` (CacheManager & RedisTemplate 동일) |
---
## 🗂 클라이언트 캐싱 정책
- SDK는 응답 헤더의 ETag를 저장했다가, 다음 요청 시 `If-None-Match` 헤더에 넣어 전송한다.
- 서버가 변경 없음을 확인하면 304 Not Modified를 반환 → 네트워크 비용 최소화.
- 권장 로직:
  - 첫 요청: 200 OK → ETag 저장
  - 두 번째 이후: ETag 포함 요청 → 304 → 로컬 캐시 사용
---
## 📚 문서 관리
- docs/testing/e2e.md — E2E 시나리오, 실행법, 이슈 기록
- docs/arch/adr-001-caching.md — 캐시 정책/직렬화기 결정
- docs/retrospectives/ — 날짜별 회고 기록
- docs/arch/adr-002-sdk-endpoint.md — SDK 엔드포인트 & 롤아웃 정책

---
## ⚠️ 표준 에러 스키마 (예시)
| 필드명        | 설명                        |
|------------|---------------------------|
| code       | 에러 코드 (예: FLAG_NOT_FOUND) |
| message    | 에러 메시지                    |
| path       | 요청 경로                     |
| timestamp  | 발생 시각 (UTC, ISO-8601)     |

---
## 🧰 Troubleshooting 요약
- E2E 테스트 중 캐시 히트 시 LinkedHashMap 으로 역직렬화되어 500 오류 발생.
  - 원인 : Jackson2JsonRedisSerializer\<Object\> 사용 시 타입 정보가 포함되지 않아 DTO 복원 실패.
  - 해결: GenericJackson2JsonRedisSerializer 로 변경 → 타입 메타데이터 포함 → DTO 안전 복원.
- k6에서 etag 헤더값을 못뽑아냄
  - 원인 : 서버는 `ResponseEntity.eTag(...)` 를 사용하므로 응답 헤더 키는 `Etag`.
  - 해결 : js에서 `res.headers["ETag"]`는 undefined 됨. 대소문자 구분 접근 필요:

---
## 개선할 점
- FeatureFlag.rulesJson: String 유지. 차후 DTO 매핑(FlagRules) 고려.
- SdkConfigController: ResponseEntity.eTag(...) → 응답 헤더 키가 ETag가 아닌 Etag. 표준인 ETag로 개선 고려.