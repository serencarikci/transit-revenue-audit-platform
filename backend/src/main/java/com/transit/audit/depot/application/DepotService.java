package com.transit.audit.depot.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transit.audit.common.concurrency.OptimisticLockSupport;
import com.transit.audit.common.exception.BusinessException;
import com.transit.audit.common.exception.ResourceNotFoundException;
import com.transit.audit.depot.domain.model.Depot;
import com.transit.audit.depot.infrastructure.persistence.DepotRepository;
import com.transit.audit.depot.web.request.CreateDepotRequest;
import com.transit.audit.depot.web.request.SetDepotActiveRequest;
import com.transit.audit.depot.web.request.UpdateDepotRequest;
import com.transit.audit.depot.web.response.DepotResponse;

@Service
public class DepotService {

	private final DepotRepository depotRepository;
	private final DepotMapper depotMapper;

	public DepotService(DepotRepository depotRepository, DepotMapper depotMapper) {
		this.depotRepository = depotRepository;
		this.depotMapper = depotMapper;
	}

	@Transactional(readOnly = true)
	public Page<DepotResponse> listDepots(Boolean active, Pageable pageable) {
		Page<Depot> page = active == null ? depotRepository.findAll(pageable)
				: depotRepository.findByActive(active, pageable);
		return page.map(depotMapper::toResponse);
	}

	@Transactional(readOnly = true)
	public DepotResponse getDepot(Long id) {
		return depotMapper.toResponse(requireDepot(id));
	}

	@Transactional
	public DepotResponse createDepot(CreateDepotRequest request) {
		if (depotRepository.existsByCode(request.code())) {
			throw new BusinessException("Conflict", "Depot code already exists: " + request.code(), 409);
		}
		Depot depot = new Depot(request.code(), request.name());
		return depotMapper.toResponse(depotRepository.save(depot));
	}

	@Transactional
	public DepotResponse updateDepot(Long id, UpdateDepotRequest request) {
		Depot depot = requireDepot(id);
		OptimisticLockSupport.requireVersion(depot.getVersion(), request.version(), "Depot");
		depot.setName(request.name());
		return depotMapper.toResponse(depotRepository.save(depot));
	}

	@Transactional
	public DepotResponse setActive(Long id, SetDepotActiveRequest request) {
		Depot depot = requireDepot(id);
		OptimisticLockSupport.requireVersion(depot.getVersion(), request.version(), "Depot");
		depot.setActive(request.active());
		return depotMapper.toResponse(depotRepository.save(depot));
	}

	private Depot requireDepot(Long id) {
		return depotRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Depot", id));
	}
}
