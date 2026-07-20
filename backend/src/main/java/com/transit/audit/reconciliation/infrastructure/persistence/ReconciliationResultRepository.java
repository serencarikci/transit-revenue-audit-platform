package com.transit.audit.reconciliation.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transit.audit.reconciliation.domain.model.ReconciliationResult;
import com.transit.audit.reconciliation.domain.model.ReconciliationStatus;

public interface ReconciliationResultRepository extends JpaRepository<ReconciliationResult, Long> {

	@Query("""
			select r from ReconciliationResult r, FinancialPeriod p
			where r.financialPeriodId = p.id
			  and (:status is null or r.status = :status)
			  and (:year is null or year(p.periodDate) = :year)
			  and (:month is null or month(p.periodDate) = :month)
			""")
	Page<ReconciliationResult> search(@Param("year") Integer year, @Param("month") Integer month,
			@Param("status") ReconciliationStatus status, Pageable pageable);
}
