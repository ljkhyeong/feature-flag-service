# E2E Segmentation Scenarios

## 목적
- SegmentEvaluator를 실제 플래그 API 흐름에 연결 검증

## 시나리오
1. `/api/flags/{id}` 조회 시 rulesJson 포함 응답 확인
2. `/sdk/v1/config` 호출 → rulesJson 파싱 후 Evaluator 적용
3. include/exclude → SegmentEvaluator 순서 검증
4. rolloutPercentage와 세그먼트 혼합 적용 검증
