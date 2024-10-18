package com.nb.imgstore.exception.dto;

import java.time.Instant;

import org.springframework.http.HttpStatusCode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nb.imgstore.exception.GlobalExceptionHandler;

import lombok.Data;

/**
 * DTO representing errors in a generic way to the client of the API.
 * <p>
 * Wrap exceptions into this DTO in {@link GlobalExceptionHandler}
 */
@Data
public class ApiError {
	@JsonIgnore
	private final HttpStatusCode statusCode;
	@JsonIgnore
	private final Exception exception;
	private final Instant timestamp;
	private final int status;
	private final String path;
	private final String message;

	public ApiError(String path, HttpStatusCode statusCode, Exception ex) {
		this(path, statusCode, ex, ex.getLocalizedMessage());
	}

	public ApiError(String path, HttpStatusCode statusCode, Exception ex, String message) {
		this.statusCode = statusCode;
		this.exception = ex;
		this.timestamp = Instant.now();
		this.status = statusCode.value();
		this.path = path;
		this.message = message;
	}

}
