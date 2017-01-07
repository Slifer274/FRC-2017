package com._604robotics.robotnik.exceptions;

public class NonExistentDataError extends Error {

	private static final long serialVersionUID = 121078116205737454L;

	public NonExistentDataError() {}

	public NonExistentDataError(String arg0) {
		super(arg0);
	}

	public NonExistentDataError(Throwable arg0) {
		super(arg0);
	}

	public NonExistentDataError(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public NonExistentDataError(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
