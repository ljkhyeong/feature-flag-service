package com.myapp.ffs.support;

import java.util.NoSuchElementException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.myapp.ffs.exception.ApplicationException;
import com.myapp.ffs.exception.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest req) {
		String detail = e.getBindingResult().getFieldErrors().stream()
			.map(err -> err.getField() + ":" + err.getDefaultMessage())
			.findFirst().orElse("validation error");

		return buildErrorResponse(ErrorCode.INVALID_METHOD_ARGUMENT, detail, req.getRequestURI());
	}

	@ExceptionHandler(ApplicationException.class)
	public ResponseEntity<ErrorResponse> handleApplicationError(ApplicationException e, HttpServletRequest req) {
		return buildErrorResponse(e.getErrorCode(), req.getRequestURI());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleNotReadableRequest(HttpMessageNotReadableException e,
		HttpServletRequest request) {
		return buildErrorResponse(ErrorCode.INVALID_REQUEST_BODY, request.getRequestURI());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleAny(Exception e, HttpServletRequest req) {
		return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), req.getRequestURI());
	}

	private ResponseEntity<ErrorResponse> buildErrorResponse(ErrorCode errorCode, String path) {
		return ResponseEntity.status(errorCode.getHttpStatus())
			.header(HttpHeaders.CACHE_CONTROL, "no-store")
			.body(ErrorResponse.of(errorCode.name(), errorCode.getMessage(), path));
	}

	private ResponseEntity<ErrorResponse> buildErrorResponse(ErrorCode errorCode, String message, String path) {
		return ResponseEntity.status(errorCode.getHttpStatus())
			.header(HttpHeaders.CACHE_CONTROL, "no-store")
			.body(ErrorResponse.of(errorCode.name(), message, path));
	}
}
