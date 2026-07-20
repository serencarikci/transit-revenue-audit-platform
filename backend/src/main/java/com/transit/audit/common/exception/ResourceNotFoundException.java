package com.transit.audit.common.exception;

public class ResourceNotFoundException extends BusinessException {

	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String entity, Object id) {
		super("Not Found", entity + " not found: " + id, 404);
	}
}
