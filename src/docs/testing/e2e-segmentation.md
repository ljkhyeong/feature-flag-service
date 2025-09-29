# E2E Segmentation Scenarios

## 목적
- SegmentEvaluator를 실제 플래그 API 흐름에 연결 검증

## 순서
1. `/api/flags/{id}` 조회 시 rulesJson 포함 응답 확인
2. `/sdk/v1/config` 호출 → rulesJson 파싱 후 Evaluator 적용
3. include/exclude → SegmentEvaluator 순서 검증
4. rolloutPercentage와 세그먼트 혼합 적용 검증

## 우선순위 규칙
1. exclude → 무조건 false
2. include → 무조건 true
3. rulesJson → 세그먼트 평가
4. rolloutPercentage → 퍼센트 롤아웃
5. baseEnabled → 기본 enabled

## 시나리오

| Case | 조건 | 기대 결과 |
|------|------|-----------|
| Exclude 우선 | userId가 exclude에 포함 | 항상 false |
| Include 우선 | userId가 include에 포함 | 항상 true |
| Segment 매치 | country=KR, device=iOS | true |
| Segment 불일치 | 조건 불일치, rollout 50% | rollout 적용 |
| Rollout=0 | 퍼센트 0 | 항상 false |
| Rollout=100 | 퍼센트 100 | 항상 true |

| Case | 조건 | 기대 결과 |
|------|------|-----------|
| OR-부분매치 | country≠KR, device∈{iOS,Android}, logic=OR | true |
| AND-실패 | country≠KR, region≠EU, logic=AND | false |
| NOT_IN | tier∉{beta,internal} | true |
| 타입 오류 | IN인데 value가 배열이 아님 | 예외 발생(ClassCastException) |
| logic 기본값 | logic 필드 없음 | AND로 처리 |
| 미지원 op | op=BETWEEN | 400/예외 (파서 단계에서 거부) |