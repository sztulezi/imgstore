package com.nb.imgstore.exception;

/**
 * Thrown when no resource is found for the id provided by the client
 */
public class NotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public NotFoundException() {
		super();
	}

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(Throwable cause) {
		super(cause);
	}

	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
