# ADR-0001 Feature Flag 플랫폼 채택 및 구조
## Context
- 개인 서비스 환경에서 점진적 롤아웃·세그먼트 기준 토글 필요
## Decision
- Control Plane(관리/저장) + Data Plane(SDK 구성 배포) 이원화
- 환경별 번들 JSON + ETag 캐싱, Redis로 번들/버전 캐시
## Consequences
- 변경 알림(버전 bump) 필요 → Pub/Sub
- SDK/서버 사이에서 정합성을 TTL+SWr(갱신 중 허용)로 보장
