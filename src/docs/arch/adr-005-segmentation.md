# ADR-005: Segmentation DSL & Evaluator 설계

## 상태
- Draft (2025-09-21)

## 배경
- 현재 Feature Flag는 include/exclude, rolloutPercentage만 지원.
- 실제 운영에서는 사용자 속성 기반 세그먼트 타깃팅 필요.
    - 예: 국가(country=KR), 디바이스(device in [iOS, Android]), 내부 계정(userId=dev*)

## 결정
1. `rulesJson` 구조 확장
   ```json
   {
     "conditions": [
       { "attribute": "country", "op": "EQUALS", "value": "KR" },
       { "attribute": "device", "op": "IN", "value": ["iOS", "Android"] }
     ],
     "logic": "AND"
   }

2. 지원 연산자
- EQUALS, NOT_EQUALS
- IN, NOT_IN
- MATCHES (정규식)
- 조합 연산: AND, OR

3. 평가 순서
- exclude → include → rulesJson → rollout → 기본 enabled

4. 파서 & 평가기
- RuleParser: JSON → Condition 객체 변환
- SegmentEvaluator: 조건 평가 실행

## 근거
- DSL 기반으로 서버/클라이언트 모두 동일한 로직 사용 가능
- 향후 확장이 용이함

## 결과
- 단위 테스트: SegmentEvaluatorTest
- E2E: /sdk/v1/config에 rulesJson 반영 검증