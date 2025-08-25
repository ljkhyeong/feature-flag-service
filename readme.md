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
