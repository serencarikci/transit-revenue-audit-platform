package com.transit.audit.terminal.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transit.audit.terminal.domain.model.TerminalDepotAssignment;

public interface TerminalDepotAssignmentRepository extends JpaRepository<TerminalDepotAssignment, Long> {

	List<TerminalDepotAssignment> findByTerminalIdOrderByValidFromDesc(Long terminalId);

	Optional<TerminalDepotAssignment> findByTerminalIdAndValidToIsNull(Long terminalId);

	@Query("""
			select a from TerminalDepotAssignment a
			where a.terminalId = :terminalId
			  and a.validFrom <= :onDate
			  and (a.validTo is null or a.validTo >= :onDate)
			""")
	Optional<TerminalDepotAssignment> findCovering(@Param("terminalId") Long terminalId,
			@Param("onDate") LocalDate onDate);
}
