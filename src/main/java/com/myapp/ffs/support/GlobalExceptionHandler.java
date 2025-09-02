package com.myapp.ffs.support;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
		String detail = e.getBindingResult().getFieldErrors().stream()
			.map(err -> err.getField() + ":" + err.getDefaultMessage())
			.findFirst().orElse("validation error");

		return ResponseEntity.badRequest()
			.body(ErrorResponse.of("VALIDATION_ERROR", "Invalid Request", detail));
	}

	@ExceptionHandler(ApplicationException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(ApplicationException e, HttpServletRequest req) {
		ErrorCode errorCode = e.getErrorCode();
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(ErrorResponse.of(errorCode.name(), errorCode.getMessage(), req.getRequestURI()));
	}


	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleAny(Exception e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ErrorResponse.of("INTERNAL_ERROR", "Unexpected Error", e.getClass().getSimpleName()));
	}
}
