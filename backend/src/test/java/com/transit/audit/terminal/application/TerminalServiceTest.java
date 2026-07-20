package com.transit.audit.terminal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.transit.audit.common.exception.BusinessException;
import com.transit.audit.common.exception.ResourceNotFoundException;
import com.transit.audit.depot.domain.model.Depot;
import com.transit.audit.depot.infrastructure.persistence.DepotRepository;
import com.transit.audit.terminal.domain.model.Terminal;
import com.transit.audit.terminal.domain.model.TerminalDepotAssignment;
import com.transit.audit.terminal.domain.model.TerminalStatus;
import com.transit.audit.terminal.infrastructure.persistence.TerminalDepotAssignmentRepository;
import com.transit.audit.terminal.infrastructure.persistence.TerminalRepository;
import com.transit.audit.terminal.web.request.CreateAssignmentRequest;
import com.transit.audit.terminal.web.request.CreateTerminalRequest;
import com.transit.audit.terminal.web.request.UpdateTerminalRequest;
import com.transit.audit.terminal.web.response.AssignmentResponse;
import com.transit.audit.terminal.web.response.TerminalResponse;

@ExtendWith(MockitoExtension.class)
class TerminalServiceTest {

	@Mock
	private TerminalRepository terminalRepository;

	@Mock
	private TerminalDepotAssignmentRepository assignmentRepository;

	@Mock
	private DepotRepository depotRepository;

	private TerminalService terminalService;

	private static final TerminalMapper TERMINAL_MAPPER = new TerminalMapper() {
		@Override
		public TerminalResponse toResponse(Terminal terminal) {
			return new TerminalResponse(terminal.getId(), terminal.getTerminalNumber(), terminal.getSerialNumber(),
					terminal.getStatus(), terminal.getLastSyncTime(), terminal.getLastTransactionTime(),
					terminal.getPendingTransactionCount(), terminal.getRetryCount(), terminal.isActive(),
					terminal.getCreatedAt(), terminal.getUpdatedAt(), terminal.getVersion());
		}

		@Override
		public AssignmentResponse toResponse(TerminalDepotAssignment assignment) {
			return new AssignmentResponse(assignment.getId(), assignment.getTerminalId(), assignment.getDepotId(),
					assignment.getValidFrom(), assignment.getValidTo(), assignment.getCreatedAt());
		}
	};

	@BeforeEach
	void setUp() {
		terminalService = new TerminalService(terminalRepository, assignmentRepository, depotRepository,
				TERMINAL_MAPPER);
	}

	@Test
	void createTerminal_savesNewTerminal_whenIdentifiersAreUnique() {
		when(terminalRepository.existsByTerminalNumber("9999")).thenReturn(false);
		when(terminalRepository.existsBySerialNumber("SN-9999")).thenReturn(false);
		when(terminalRepository.save(any(Terminal.class))).thenAnswer(invocation -> invocation.getArgument(0));

		TerminalResponse response = terminalService.createTerminal(new CreateTerminalRequest("9999", "SN-9999"));

		assertThat(response.terminalNumber()).isEqualTo("9999");
		assertThat(response.status()).isEqualTo(TerminalStatus.HEALTHY);
		assertThat(response.active()).isTrue();
	}

	@Test
	void createTerminal_throwsConflict_whenTerminalNumberExists() {
		when(terminalRepository.existsByTerminalNumber("3122")).thenReturn(true);

		assertThatThrownBy(() -> terminalService.createTerminal(new CreateTerminalRequest("3122", "SN-X")))
				.isInstanceOf(BusinessException.class).hasMessageContaining("3122");
	}

	@Test
	void updateTerminal_throwsConflict_whenVersionIsStale() {
		Terminal terminal = terminalWithVersion(1L, "3122", "SN-3122", 3L);
		when(terminalRepository.findById(1L)).thenReturn(Optional.of(terminal));

		assertThatThrownBy(() -> terminalService.updateTerminal(1L,
				new UpdateTerminalRequest("SN-3122", TerminalStatus.OFFLINE, 2L))).isInstanceOf(BusinessException.class)
				.hasMessageContaining("changed by another user");
	}

	@Test
	void getTerminal_throwsNotFound_whenMissing() {
		when(terminalRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> terminalService.getTerminal(99L)).isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void findDepotForTerminalAt_returnsHistoricalDepot_notCurrentOpenAssignment() {

		Terminal terminal = terminalWithVersion(10L, "3122", "SN-3122", 0L);
		when(terminalRepository.findById(10L)).thenReturn(Optional.of(terminal));

		Depot nel = depot(1L, "NEL");
		Depot whr = depot(2L, "WHR");

		TerminalDepotAssignment marchNel = assignment(100L, 10L, 1L, LocalDate.of(2026, 3, 1),
				LocalDate.of(2026, 3, 31));
		TerminalDepotAssignment aprilWhr = assignment(101L, 10L, 2L, LocalDate.of(2026, 4, 1), null);

		when(assignmentRepository.findCovering(10L, LocalDate.of(2026, 3, 15))).thenReturn(Optional.of(marchNel));
		when(assignmentRepository.findCovering(10L, LocalDate.of(2026, 4, 10))).thenReturn(Optional.of(aprilWhr));
		when(depotRepository.findById(1L)).thenReturn(Optional.of(nel));
		when(depotRepository.findById(2L)).thenReturn(Optional.of(whr));

		Instant midMarch = Instant.parse("2026-03-15T12:00:00Z");
		Instant midApril = Instant.parse("2026-04-10T08:00:00Z");

		assertThat(terminalService.findDepotForTerminalAt(10L, midMarch)).contains(nel);
		assertThat(terminalService.findDepotForTerminalAt(10L, midApril)).contains(whr);
	}

	@Test
	void findDepotForTerminalAt_returnsEmpty_whenNoAssignmentCoversTransactionDate() {
		Terminal terminal = terminalWithVersion(10L, "3122", "SN-3122", 0L);
		when(terminalRepository.findById(10L)).thenReturn(Optional.of(terminal));
		when(assignmentRepository.findCovering(10L, LocalDate.of(2025, 12, 1))).thenReturn(Optional.empty());

		assertThat(terminalService.findDepotForTerminalAt(10L, Instant.parse("2025-12-01T00:00:00Z"))).isEmpty();
	}

	@Test
	void createAssignment_closesOpenAssignmentTheDayBeforeNewValidFrom() {
		Terminal terminal = terminalWithVersion(10L, "3122", "SN-3122", 0L);
		when(terminalRepository.findById(10L)).thenReturn(Optional.of(terminal));
		when(depotRepository.findById(2L)).thenReturn(Optional.of(depot(2L, "WHR")));

		TerminalDepotAssignment open = assignment(100L, 10L, 1L, LocalDate.of(2026, 3, 1), null);
		when(assignmentRepository.findByTerminalIdAndValidToIsNull(10L)).thenReturn(Optional.of(open));
		when(assignmentRepository.save(any(TerminalDepotAssignment.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		AssignmentResponse response = terminalService.createAssignment(10L,
				new CreateAssignmentRequest(2L, LocalDate.of(2026, 4, 1)));

		assertThat(open.getValidTo()).isEqualTo(LocalDate.of(2026, 3, 31));
		assertThat(response.depotId()).isEqualTo(2L);
		assertThat(response.validFrom()).isEqualTo(LocalDate.of(2026, 4, 1));
		assertThat(response.validTo()).isNull();

		ArgumentCaptor<TerminalDepotAssignment> captor = ArgumentCaptor.forClass(TerminalDepotAssignment.class);
		verify(assignmentRepository, times(2)).save(captor.capture());
		assertThat(captor.getAllValues().get(1).getDepotId()).isEqualTo(2L);
	}

	private static Terminal terminalWithVersion(Long id, String number, String serial, Long version) {
		Terminal terminal = new Terminal(number, serial);
		ReflectionTestUtils.setField(terminal, "id", id);
		ReflectionTestUtils.setField(terminal, "version", version);
		return terminal;
	}

	private static Depot depot(Long id, String code) {
		Depot depot = new Depot(code, code + " Depot");
		ReflectionTestUtils.setField(depot, "id", id);
		return depot;
	}

	private static TerminalDepotAssignment assignment(Long id, Long terminalId, Long depotId, LocalDate from,
			LocalDate to) {
		TerminalDepotAssignment assignment = new TerminalDepotAssignment(terminalId, depotId, from);
		ReflectionTestUtils.setField(assignment, "id", id);
		if (to != null) {
			assignment.closeOn(to);
		}
		return assignment;
	}
}
