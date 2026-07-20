package com.transit.audit.terminal.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.transit.audit.depot.domain.model.Depot;
import com.transit.audit.depot.infrastructure.persistence.DepotRepository;
import com.transit.audit.support.AbstractPostgresIntegrationTest;
import com.transit.audit.terminal.domain.model.Terminal;
import com.transit.audit.terminal.domain.model.TerminalDepotAssignment;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class TerminalRepositoryIT extends AbstractPostgresIntegrationTest {

	@Autowired
	private TerminalRepository terminalRepository;

	@Autowired
	private TerminalDepotAssignmentRepository assignmentRepository;

	@Autowired
	private DepotRepository depotRepository;

	@Test
	void savesTerminalAndFindsItById() {
		Terminal saved = terminalRepository.save(new Terminal("T900", "SN-T900"));

		assertThat(terminalRepository.findById(saved.getId())).isPresent();
		assertThat(terminalRepository.existsByTerminalNumber("T900")).isTrue();
		assertThat(terminalRepository.existsBySerialNumber("SN-T900")).isTrue();
	}

	@Test
	void findCovering_resolvesHistoricalDepotAssignmentByTransactionDate() {
		Depot first = depotRepository.save(new Depot("D90", "First Depot"));
		Depot second = depotRepository.save(new Depot("D91", "Second Depot"));
		Terminal terminal = terminalRepository.save(new Terminal("T901", "SN-T901"));

		TerminalDepotAssignment past = new TerminalDepotAssignment(terminal.getId(), first.getId(),
				LocalDate.of(2026, 1, 1));
		past.closeOn(LocalDate.of(2026, 1, 31));
		assignmentRepository.save(past);

		assignmentRepository
				.save(new TerminalDepotAssignment(terminal.getId(), second.getId(), LocalDate.of(2026, 2, 1)));

		assertThat(assignmentRepository.findCovering(terminal.getId(), LocalDate.of(2026, 1, 15))).get()
				.extracting(TerminalDepotAssignment::getDepotId).isEqualTo(first.getId());

		assertThat(assignmentRepository.findCovering(terminal.getId(), LocalDate.of(2026, 2, 15))).get()
				.extracting(TerminalDepotAssignment::getDepotId).isEqualTo(second.getId());

		assertThat(assignmentRepository.findCovering(terminal.getId(), LocalDate.of(2025, 12, 31))).isEmpty();
	}
}
