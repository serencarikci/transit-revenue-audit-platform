package com.transit.audit.transaction.web;

import java.time.Instant;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.transit.audit.transaction.application.TransactionImportService;
import com.transit.audit.transaction.application.TransactionService;
import com.transit.audit.transaction.domain.model.TransactionType;
import com.transit.audit.transaction.web.response.TransactionImportResult;
import com.transit.audit.transaction.web.response.TransactionResponse;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions")
public class TransactionController {

	private final TransactionService transactionService;
	private final TransactionImportService transactionImportService;

	public TransactionController(TransactionService transactionService,
			TransactionImportService transactionImportService) {
		this.transactionService = transactionService;
		this.transactionImportService = transactionImportService;
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public PagedModel<TransactionResponse> search(@RequestParam(required = false) Long terminalId,
			@RequestParam(required = false) TransactionType type, @RequestParam(required = false) Instant from,
			@RequestParam(required = false) Instant to, Pageable pageable) {
		return new PagedModel<>(transactionService.search(terminalId, type, from, to, pageable));
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public TransactionResponse get(@PathVariable Long id) {
		return transactionService.get(id);
	}

	@PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("hasAnyRole('ADMIN','FINANCE_USER','OPERATIONS_USER')")
	public TransactionImportResult importCsv(@RequestParam("file") MultipartFile file) {
		return transactionImportService.importCsv(file);
	}
}
