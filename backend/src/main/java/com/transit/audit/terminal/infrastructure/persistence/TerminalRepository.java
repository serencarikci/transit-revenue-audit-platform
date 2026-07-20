package com.transit.audit.terminal.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.transit.audit.terminal.domain.model.Terminal;

public interface TerminalRepository extends JpaRepository<Terminal, Long> {

	boolean existsByTerminalNumber(String terminalNumber);

	boolean existsBySerialNumber(String serialNumber);

	Optional<Terminal> findByTerminalNumber(String terminalNumber);

	Page<Terminal> findByActive(boolean active, Pageable pageable);
}
