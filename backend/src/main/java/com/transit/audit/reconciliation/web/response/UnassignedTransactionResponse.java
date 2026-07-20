package com.transit.audit.reconciliation.web.response;

public record UnassignedTransactionResponse(Long transactionId, Long terminalId, String transactionReference,
		String transactionTime) {
}
