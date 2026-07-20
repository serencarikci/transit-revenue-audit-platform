package com.transit.audit.transaction.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transit.audit.transaction.domain.model.CardTransaction;

public interface TransactionRepository
		extends JpaRepository<CardTransaction, Long>, JpaSpecificationExecutor<CardTransaction> {

	boolean existsByApprovalNumberAndCardAliasAndTerminalIdAndAmount(String approvalNumber, String cardAlias,
			Long terminalId, BigDecimal amount);

	boolean existsByTransactionReference(String transactionReference);

	@Query("""
			select t from CardTransaction t
			where t.transactionTime >= :epochStart and t.transactionTime < :epochEnd
			""")
	List<CardTransaction> findEpochDatedTransactions(@Param("epochStart") Instant epochStart,
			@Param("epochEnd") Instant epochEnd);

	@Query("""
			select t from CardTransaction t
			where t.transactionTime >= :fromTime and t.transactionTime <= :toTime
			""")
	List<CardTransaction> findByTransactionTimeBetween(@Param("fromTime") Instant fromTime,
			@Param("toTime") Instant toTime);
}
