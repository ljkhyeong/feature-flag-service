package com.myapp.ffs.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
	FLAG_NOT_FOUND(HttpStatus.NOT_FOUND, "FLAG를 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;

}
