package com.transit.audit.transaction.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transit.audit.transaction.domain.model.CardTransaction;
import com.transit.audit.transaction.domain.model.TransactionType;

public interface TransactionRepository extends JpaRepository<CardTransaction, Long> {

	boolean existsByApprovalNumberAndCardAliasAndTerminalIdAndAmount(String approvalNumber, String cardAlias,
			Long terminalId, BigDecimal amount);

	boolean existsByTransactionReference(String transactionReference);

	@Query("""
			select t from CardTransaction t
			where (:terminalId is null or t.terminalId = :terminalId)
			  and (:type is null or t.transactionType = :type)
			  and (:fromTime is null or t.transactionTime >= :fromTime)
			  and (:toTime is null or t.transactionTime <= :toTime)
			""")
	Page<CardTransaction> search(@Param("terminalId") Long terminalId, @Param("type") TransactionType type,
			@Param("fromTime") Instant fromTime, @Param("toTime") Instant toTime, Pageable pageable);

	@Query("""
			select t from CardTransaction t
			where t.transactionTime >= :epochStart and t.transactionTime < :epochEnd
			""")
	List<CardTransaction> findEpochDatedTransactions(@Param("epochStart") Instant epochStart,
			@Param("epochEnd") Instant epochEnd);

	List<CardTransaction> findByTerminalIdAndTransactionTimeBetween(Long terminalId, Instant from, Instant to);

	@Query("""
			select t from CardTransaction t
			where t.transactionTime >= :fromTime and t.transactionTime <= :toTime
			""")
	List<CardTransaction> findByTransactionTimeBetween(@Param("fromTime") Instant fromTime,
			@Param("toTime") Instant toTime);
}
