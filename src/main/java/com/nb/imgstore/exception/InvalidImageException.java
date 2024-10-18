package com.nb.imgstore.exception;

/**
 * Thrown when the client tries to upload an invalid image
 */
public class InvalidImageException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidImageException() {
		super();
	}

	public InvalidImageException(String message) {
		super(message);
	}

	public InvalidImageException(Throwable cause) {
		super(cause);
	}

	public InvalidImageException(String message, Throwable cause) {
		super(message, cause);
	}

}
