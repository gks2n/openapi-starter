package com.eaglebank.bankapi.error;

public class UnprocessableEntityException extends RuntimeException {
	public UnprocessableEntityException(String message) {
		super(message);
	}
}
