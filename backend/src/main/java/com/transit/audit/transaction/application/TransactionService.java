package com.transit.audit.transaction.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transit.audit.common.exception.ResourceNotFoundException;
import com.transit.audit.transaction.domain.model.CardTransaction;
import com.transit.audit.transaction.domain.model.TransactionType;
import com.transit.audit.transaction.infrastructure.persistence.TransactionRepository;
import com.transit.audit.transaction.web.response.TransactionResponse;

import jakarta.persistence.criteria.Predicate;

@Service
public class TransactionService {

	public static final Instant EPOCH_DAY_START = Instant.parse("1970-01-01T00:00:00Z");
	public static final Instant EPOCH_DAY_END = Instant.parse("1970-01-02T00:00:00Z");

	private final TransactionRepository transactionRepository;
	private final TransactionMapper transactionMapper;

	public TransactionService(TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
		this.transactionRepository = transactionRepository;
		this.transactionMapper = transactionMapper;
	}

	@Transactional(readOnly = true)
	public Page<TransactionResponse> search(Long terminalId, TransactionType type, Instant from, Instant to,
			Pageable pageable) {
		Specification<CardTransaction> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (terminalId != null) {
				predicates.add(cb.equal(root.get("terminalId"), terminalId));
			}
			if (type != null) {
				predicates.add(cb.equal(root.get("transactionType"), type));
			}
			if (from != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("transactionTime"), from));
			}
			if (to != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("transactionTime"), to));
			}
			return cb.and(predicates.toArray(Predicate[]::new));
		};
		return transactionRepository.findAll(spec, pageable).map(transactionMapper::toResponse);
	}

	@Transactional(readOnly = true)
	public TransactionResponse get(Long id) {
		CardTransaction tx = transactionRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("CardTransaction", id));
		return transactionMapper.toResponse(tx);
	}

	static boolean isEpochDated(Instant transactionTime) {
		LocalDate date = transactionTime.atZone(ZoneOffset.UTC).toLocalDate();
		return LocalDate.of(1970, 1, 1).equals(date);
	}
}
