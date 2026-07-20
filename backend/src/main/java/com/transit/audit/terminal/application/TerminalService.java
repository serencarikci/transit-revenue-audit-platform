package com.transit.audit.terminal.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transit.audit.common.concurrency.OptimisticLockSupport;
import com.transit.audit.common.exception.BusinessException;
import com.transit.audit.common.exception.ResourceNotFoundException;
import com.transit.audit.depot.domain.model.Depot;
import com.transit.audit.depot.infrastructure.persistence.DepotRepository;
import com.transit.audit.terminal.domain.model.Terminal;
import com.transit.audit.terminal.domain.model.TerminalDepotAssignment;
import com.transit.audit.terminal.infrastructure.persistence.TerminalDepotAssignmentRepository;
import com.transit.audit.terminal.infrastructure.persistence.TerminalRepository;
import com.transit.audit.terminal.web.request.CloseAssignmentRequest;
import com.transit.audit.terminal.web.request.CreateAssignmentRequest;
import com.transit.audit.terminal.web.request.CreateTerminalRequest;
import com.transit.audit.terminal.web.request.SetTerminalActiveRequest;
import com.transit.audit.terminal.web.request.UpdateTerminalRequest;
import com.transit.audit.terminal.web.response.AssignmentResponse;
import com.transit.audit.terminal.web.response.TerminalResponse;

@Service
public class TerminalService {

	private final TerminalRepository terminalRepository;
	private final TerminalDepotAssignmentRepository assignmentRepository;
	private final DepotRepository depotRepository;
	private final TerminalMapper terminalMapper;

	public TerminalService(TerminalRepository terminalRepository,
			TerminalDepotAssignmentRepository assignmentRepository, DepotRepository depotRepository,
			TerminalMapper terminalMapper) {
		this.terminalRepository = terminalRepository;
		this.assignmentRepository = assignmentRepository;
		this.depotRepository = depotRepository;
		this.terminalMapper = terminalMapper;
	}

	@Transactional(readOnly = true)
	public Page<TerminalResponse> listTerminals(Boolean active, Pageable pageable) {
		Page<Terminal> page = active == null ? terminalRepository.findAll(pageable)
				: terminalRepository.findByActive(active, pageable);
		return page.map(terminalMapper::toResponse);
	}

	@Transactional(readOnly = true)
	public TerminalResponse getTerminal(Long id) {
		return terminalMapper.toResponse(requireTerminal(id));
	}

	@Transactional
	public TerminalResponse createTerminal(CreateTerminalRequest request) {
		if (terminalRepository.existsByTerminalNumber(request.terminalNumber())) {
			throw new BusinessException("Conflict", "Terminal number already exists: " + request.terminalNumber(), 409);
		}
		if (terminalRepository.existsBySerialNumber(request.serialNumber())) {
			throw new BusinessException("Conflict", "Terminal serial number already exists: " + request.serialNumber(),
					409);
		}
		Terminal terminal = new Terminal(request.terminalNumber(), request.serialNumber());
		return terminalMapper.toResponse(terminalRepository.save(terminal));
	}

	@Transactional
	public TerminalResponse updateTerminal(Long id, UpdateTerminalRequest request) {
		Terminal terminal = requireTerminal(id);
		OptimisticLockSupport.requireVersion(terminal.getVersion(), request.version(), "Terminal");
		if (!terminal.getSerialNumber().equals(request.serialNumber())
				&& terminalRepository.existsBySerialNumber(request.serialNumber())) {
			throw new BusinessException("Conflict", "Terminal serial number already exists: " + request.serialNumber(),
					409);
		}
		terminal.setSerialNumber(request.serialNumber());
		terminal.setStatus(request.status());
		return terminalMapper.toResponse(terminalRepository.save(terminal));
	}

	@Transactional
	public TerminalResponse setActive(Long id, SetTerminalActiveRequest request) {
		Terminal terminal = requireTerminal(id);
		OptimisticLockSupport.requireVersion(terminal.getVersion(), request.version(), "Terminal");
		terminal.setActive(request.active());
		return terminalMapper.toResponse(terminalRepository.save(terminal));
	}

	@Transactional(readOnly = true)
	public List<AssignmentResponse> listAssignments(Long terminalId) {
		requireTerminal(terminalId);
		return assignmentRepository.findByTerminalIdOrderByValidFromDesc(terminalId).stream()
				.map(terminalMapper::toResponse).toList();
	}



	@Transactional
	public AssignmentResponse createAssignment(Long terminalId, CreateAssignmentRequest request) {
		requireTerminal(terminalId);
		requireDepot(request.depotId());

		assignmentRepository.findByTerminalIdAndValidToIsNull(terminalId)
				.ifPresent(open -> closeOpenAssignment(open, request.validFrom().minusDays(1)));

		TerminalDepotAssignment assignment = new TerminalDepotAssignment(terminalId, request.depotId(),
				request.validFrom());
		return terminalMapper.toResponse(assignmentRepository.save(assignment));
	}

	@Transactional
	public AssignmentResponse closeOpenAssignment(Long terminalId, CloseAssignmentRequest request) {
		requireTerminal(terminalId);
		TerminalDepotAssignment open = assignmentRepository.findByTerminalIdAndValidToIsNull(terminalId)
				.orElseThrow(() -> new BusinessException("Conflict",
						"No open depot assignment exists for terminal " + terminalId, 409));
		closeOpenAssignment(open, request.validTo());
		return terminalMapper.toResponse(assignmentRepository.save(open));
	}



	@Transactional(readOnly = true)
	public Optional<Depot> findDepotForTerminalAt(Long terminalId, Instant transactionTime) {
		requireTerminal(terminalId);
		LocalDate onDate = transactionTime.atZone(ZoneOffset.UTC).toLocalDate();
		return assignmentRepository.findCovering(terminalId, onDate)
				.flatMap(assignment -> depotRepository.findById(assignment.getDepotId()));
	}

	private void closeOpenAssignment(TerminalDepotAssignment open, LocalDate validTo) {
		if (validTo.isBefore(open.getValidFrom())) {
			throw new BusinessException("Bad Request",
					"validTo must be on or after validFrom (" + open.getValidFrom() + ")", 400);
		}
		open.closeOn(validTo);
		assignmentRepository.save(open);
	}

	private Terminal requireTerminal(Long id) {
		return terminalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Terminal", id));
	}

	private Depot requireDepot(Long id) {
		return depotRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Depot", id));
	}
}
