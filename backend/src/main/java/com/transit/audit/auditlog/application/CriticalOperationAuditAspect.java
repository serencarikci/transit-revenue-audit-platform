package com.transit.audit.auditlog.application;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.transit.audit.anomaly.web.response.AnomalyResponse;
import com.transit.audit.identity.web.response.UserResponse;
import com.transit.audit.reconciliation.web.response.ReconciliationResultResponse;

@Aspect
@Component
public class CriticalOperationAuditAspect {

	private final AuditLogService auditLogService;

	public CriticalOperationAuditAspect(AuditLogService auditLogService) {
		this.auditLogService = auditLogService;
	}

	@AfterReturning(pointcut = "execution(* com.transit.audit.identity.application.UserService.changeRole(..))", returning = "result")
	public void afterRoleChange(UserResponse result) {
		auditLogService.record("USER_ROLE_CHANGE", "User", String.valueOf(result.id()), currentUser(),
				"{\"role\":\"" + result.role() + "\"}");
	}

	@AfterReturning(pointcut = "execution(* com.transit.audit.reconciliation.application.ReconciliationService.calculate(..))", returning = "result")
	public void afterReconciliationCalculate(ReconciliationResultResponse result) {
		auditLogService.record("RECONCILIATION_CALCULATE", "ReconciliationResult", String.valueOf(result.id()),
				currentUser(), "{\"status\":\"" + result.status() + "\",\"variance\":\"" + result.variance() + "\"}");
	}

	@AfterReturning(pointcut = "execution(* com.transit.audit.reconciliation.application.ReconciliationService.resolve(..))", returning = "result")
	public void afterVarianceResolve(ReconciliationResultResponse result) {
		auditLogService.record("RECONCILIATION_RESOLVE", "ReconciliationResult", String.valueOf(result.id()),
				currentUser(), "{\"note\":\"" + safe(result.resolutionNote()) + "\"}");
	}

	@AfterReturning(pointcut = "execution(* com.transit.audit.anomaly.application.AnomalyService.resolve(..))", returning = "result")
	public void afterAnomalyResolve(AnomalyResponse result) {
		auditLogService.record("ANOMALY_RESOLVE", "Anomaly", String.valueOf(result.id()), currentUser(),
				"{\"note\":\"" + safe(result.resolutionNote()) + "\"}");
	}

	private String currentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth == null ? "system" : auth.getName();
	}

	private String safe(String value) {
		return value == null ? "" : value.replace("\"", "'");
	}
}
