package com.myapp.ffs.support;

public record ErrorResponse(String code, String message, String detail) {
	public static ErrorResponse of(String code, String message, String detail) {
		return new ErrorResponse(code, message, detail);
	}
}
