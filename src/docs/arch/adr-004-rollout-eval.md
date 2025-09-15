# ADR-004: RolloutEvaluator 분포 검증 정책

## 상태
Accepted (2025-09-14)

## 배경
- 퍼센트 롤아웃 기능은 사용자 ID 해싱 후 버킷팅으로 구현.
- 신뢰성을 위해 표본 분포가 rolloutPercentage와 일치해야 함.

## 결정
- 표본 수 10,000 기준 허용 오차 ±1% 이내로 수렴해야 한다.
- JUnit 테스트(`RolloutEvaluatorDistributionTest`)로 이를 자동 검증한다.

## 근거
- 해시 기반 버킷팅은 통계적 안정성을 갖추고 있음을 검증함.

## 결과
- 분포 검증 테스트 3회 반복 결과 모두 허용 오차 내.
