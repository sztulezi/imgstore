package com.nb.imgstore.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.nb.imgstore.exception.dto.ApiError;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiError> handleMethodArgumentNotValidException(HttpServletRequest req, MethodArgumentNotValidException ex) {
		String msg = "Validation failed: " + ex.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(" | "));
		ApiError apiError = new ApiError(req.getRequestURI(), HttpStatus.BAD_REQUEST, ex, msg);
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<ApiError> handleConstraintViolationException(HttpServletRequest req, Exception ex) {
		ApiError apiError = new ApiError(req.getRequestURI(), HttpStatus.BAD_REQUEST, ex);
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(InvalidImageException.class)
	ResponseEntity<ApiError> handleInvalidImageException(HttpServletRequest req, Exception ex) {
		ApiError apiError = new ApiError(req.getRequestURI(), HttpStatus.BAD_REQUEST, ex);
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(NotFoundException.class)
	ResponseEntity<ApiError> handleNotFoundException(HttpServletRequest req, Exception ex) {
		ApiError apiError = new ApiError(req.getRequestURI(), HttpStatus.NOT_FOUND, ex);
		return buildResponseEntity(apiError);
	}

	private ResponseEntity<ApiError> buildResponseEntity(ApiError apiError) {
		log.warn("Client error response: {}", apiError, apiError.getException());
		return new ResponseEntity<>(apiError, apiError.getStatusCode());
	}

}
