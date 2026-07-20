package com.transit.audit.common.exception;

public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String title;
	private final int status;

	public BusinessException(String title, String detail, int status) {
		super(detail);
		this.title = title;
		this.status = status;
	}

	public String getTitle() {
		return title;
	}

	public int getStatus() {
		return status;
	}
}
