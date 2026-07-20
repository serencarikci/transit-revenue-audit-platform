package com.transit.audit.transaction.web.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.transit.audit.transaction.domain.model.TransactionType;

public record TransactionResponse(Long id, String transactionReference, String approvalNumber, String cardAlias,
		Long terminalId, TransactionType transactionType, String productType, BigDecimal amount,
		Instant transactionTime, Instant createdAt, String sourceSystem) {
}
