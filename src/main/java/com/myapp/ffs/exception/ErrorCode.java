package com.myapp.ffs.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
	FLAG_NOT_FOUND(HttpStatus.NOT_FOUND, "FLAG를 찾을 수 없습니다."),
	INVALID_METHOD_ARGUMENT(HttpStatus.BAD_REQUEST, "유효하지 않은 Argument입니다."),
	INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러");

	private final HttpStatus httpStatus;
	private final String message;

}
