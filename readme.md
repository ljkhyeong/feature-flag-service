# Feature Flag Service
목표: 
- 세그먼트 타깃팅 - 사용자 그룹별로 기능을 다르게 적용 
- 퍼센트 롤아웃 - 전체 사용자 중 일부 비율에게만 새로운 기능을 배포 (점진적 테스트, 롤백 가능)
- SDK 구성 제공 - 배포없이 실시간으로 설정값을 서버에서 내려줌

아키텍처(초안)
- Control Plane: Admin API (Spring Boot) + MySQL(@Version)
- Data Plane: /sdk/v1/config (ETag, gzip) + Redis 캐시 + Pub/Sub 버전 bump

품질/운영: Testcontainers, REST Docs, Actuator/Micrometer, Docker Compose

MVP 기능: 플래그/변형 CRUD, 환경별 번들 JSON, ETag, 감사로그

📄 API 문서: [HTML 보기](build/docs/asciidoc/index.html)

# API 성능 테스트 결과

## 1. 부하 테스트 툴: K6

### 테스트 설정
- **테스트 시간**: 30초
- **동시 사용자 수 (VUs)**: 10명

### 주요 성능 지표 (캐싱 전 / 캐싱 후)
- **평균 응답 시간 (Average response time)**: 27.65ms / 11.43ms
- **90th percentile 지연 (p90 latency)**: 46.1ms / 21.14ms
- **95th percentile 지연 (p95 latency)**: 62.48ms / 36.12ms
- **총 요청 수**: 300 요청
- **성공률**: 100%
- **테스트 완료 시간**: 30초

### 결론
- 평균 응답 시간이 200ms로, 일반적인 API 요청에 적합한 성능을 보였습니다.
- p95 지연이 500ms로, 95%의 요청은 500ms 내에 완료되었습니다.
- 전체 요청에 대해 100%의 성공률을 기록했습니다.