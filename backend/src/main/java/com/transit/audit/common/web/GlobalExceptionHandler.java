package com.transit.audit.common.web;

import java.net.URI;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.transit.audit.common.exception.BusinessException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ProblemDetail handleBusiness(BusinessException ex, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(ex.getStatus()), ex.getMessage());
		problem.setTitle(ex.getTitle());
		problem.setInstance(URI.create(request.getRequestURI()));
		return problem;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		String detail = ex.getBindingResult().getFieldErrors().stream().map(this::formatFieldError)
				.collect(Collectors.joining(", "));
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
		problem.setTitle("Validation Failed");
		problem.setInstance(URI.create(request.getRequestURI()));
		return problem;
	}

	@ExceptionHandler(AuthenticationException.class)
	public ProblemDetail handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
				"Invalid username or password");
		problem.setTitle("Unauthorized");
		problem.setInstance(URI.create(request.getRequestURI()));
		return problem;
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access denied");
		problem.setTitle("Forbidden");
		problem.setInstance(URI.create(request.getRequestURI()));
		return problem;
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
				"Unexpected server error");
		problem.setTitle("Internal Server Error");
		problem.setInstance(URI.create(request.getRequestURI()));
		return problem;
	}

	private String formatFieldError(FieldError error) {
		return error.getField() + ": " + error.getDefaultMessage();
	}
}
