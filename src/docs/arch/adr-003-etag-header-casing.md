# ADR-003: ETag 헤더 케이스 정책 (Etag vs ETag)

## 상태
Accepted (2025-09-13)

## 배경
- Spring `ResponseEntity.eTag(...)` 사용 시 응답 헤더 키는 `Etag`.
- 일부 툴(k6 등)은 헤더 키를 **케이스 민감**하게 다루어 `ETag`로 접근 시 값을 읽지 못하는 사례 발생.

## 결정
- **서버는 `ResponseEntity#eTag(...)` 유지** → 응답 키는 `Etag`.
- 클라이언트/툴(k6, SDK)은 **헤더 이름을 대소문자 무시(case-insensitive)** 로 취급하여 읽는다.

## 근거
- HTTP 헤더는 스펙상 **대소문자 비구분**.
- 구현 일관성 유지 + 클라이언트 쪽에서 범용적으로 안전 처리.

## 가이드
- k6: `res.headers`에서 키를 소문자 비교로 탐색하거나 `res.headers["Etag"]` 사용.
- Postman/curl은 기본 동작으로 문제 없음.
- 추후 혼동 방지를 위해 `ResponseEntity.header(HttpHeaders.ETAG, "\"" + tag + "\"")`로 전환 고려중
