package com.transit.audit.depot.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.transit.audit.depot.domain.model.Depot;

public interface DepotRepository extends JpaRepository<Depot, Long> {

	boolean existsByCode(String code);

	Page<Depot> findByActive(boolean active, Pageable pageable);
}
