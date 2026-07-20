package com.transit.audit.reconciliation.infrastructure.persistence;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.transit.audit.reconciliation.domain.model.FinancialPeriod;

public interface FinancialPeriodRepository extends JpaRepository<FinancialPeriod, Long> {

	boolean existsByDepotIdAndPeriodDate(Long depotId, LocalDate periodDate);

	Optional<FinancialPeriod> findByDepotIdAndPeriodDate(Long depotId, LocalDate periodDate);
}
