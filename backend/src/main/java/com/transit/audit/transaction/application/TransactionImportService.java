package com.transit.audit.transaction.application;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.transit.audit.common.exception.BusinessException;
import com.transit.audit.common.upload.CsvUploadValidator;
import com.transit.audit.common.util.CardAliasMasker;
import com.transit.audit.terminal.domain.model.Terminal;
import com.transit.audit.terminal.infrastructure.persistence.TerminalRepository;
import com.transit.audit.transaction.domain.model.CardTransaction;
import com.transit.audit.transaction.domain.model.TransactionType;
import com.transit.audit.transaction.infrastructure.persistence.TransactionRepository;
import com.transit.audit.transaction.web.response.TransactionImportResult;

@Service
public class TransactionImportService {

	private static final Logger log = LoggerFactory.getLogger(TransactionImportService.class);

	static final String EXPECTED_HEADER = "transactionReference,approvalNumber,cardAlias,terminalNumber,transactionType,productType,amount,transactionTime";

	private final TransactionRepository transactionRepository;
	private final TerminalRepository terminalRepository;

	public TransactionImportService(TransactionRepository transactionRepository,
			TerminalRepository terminalRepository) {
		this.transactionRepository = transactionRepository;
		this.terminalRepository = terminalRepository;
	}

	@Transactional
	public TransactionImportResult importCsv(MultipartFile file) {
		CsvUploadValidator.validate(file, EXPECTED_HEADER);

		int total = 0;
		int imported = 0;
		int duplicates = 0;
		int epochDated = 0;
		List<String> errors = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
			String header = reader.readLine();
			if (header == null) {
				throw new BusinessException("Bad Request", "CSV file is empty", 400);
			}
			String line;
			int lineNo = 1;
			while ((line = reader.readLine()) != null) {
				lineNo++;
				if (line.isBlank()) {
					continue;
				}
				total++;
				try {
					ImportOutcome outcome = importLine(line, lineNo);
					switch (outcome) {
					case IMPORTED -> imported++;
					case DUPLICATE -> duplicates++;
					case EPOCH_IMPORTED -> {
						imported++;
						epochDated++;
					}
					}
				} catch (BusinessException ex) {
					errors.add("Line " + lineNo + ": " + ex.getMessage());
				} catch (RuntimeException ex) {
					errors.add("Line " + lineNo + ": " + ex.getMessage());
				}
			}
		} catch (BusinessException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new BusinessException("Bad Request", "Failed to read CSV: " + ex.getMessage(), 400);
		}

		log.info("CSV import finished: total={}, imported={}, duplicates={}, epochDated={}, errors={}", total, imported,
				duplicates, epochDated, errors.size());
		return new TransactionImportResult(total, imported, duplicates, epochDated, List.copyOf(errors));
	}

	private ImportOutcome importLine(String line, int lineNo) {
		String[] cols = line.split(",", -1);
		if (cols.length < 8) {
			throw new BusinessException("Bad Request", "Row must have at least 8 columns", 400);
		}

		String reference = requireText(cols[0], "transactionReference");
		String approval = requireText(cols[1], "approvalNumber");
		String cardAlias = requireText(cols[2], "cardAlias");
		String terminalNumber = requireText(cols[3], "terminalNumber");
		TransactionType type;
		try {
			type = TransactionType.valueOf(cols[4].trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			throw new BusinessException("Bad Request", "Invalid transactionType: " + cols[4].trim(), 400);
		}
		String productType = cols[5].trim().isEmpty() ? null : cols[5].trim();
		BigDecimal amount;
		try {
			amount = new BigDecimal(cols[6].trim());
		} catch (NumberFormatException ex) {
			throw new BusinessException("Bad Request", "Invalid amount: " + cols[6].trim(), 400);
		}
		if (amount.signum() < 0) {
			throw new BusinessException("Bad Request", "Amount must be 0 or more", 400);
		}
		Instant transactionTime;
		try {
			transactionTime = Instant.parse(cols[7].trim());
		} catch (Exception ex) {
			throw new BusinessException("Bad Request", "Invalid transactionTime. Use ISO-8601 format", 400);
		}
		String sourceSystem = cols.length > 8 && !cols[8].trim().isEmpty() ? cols[8].trim() : "CSV_IMPORT";

		Terminal terminal = terminalRepository.findByTerminalNumber(terminalNumber).orElseThrow(
				() -> new BusinessException("Not Found", "Unknown terminalNumber: " + terminalNumber, 404));

		if (transactionRepository.existsByTransactionReference(reference)) {
			log.warn("Skipping duplicate reference={} cardAlias={}", reference, CardAliasMasker.mask(cardAlias));
			return ImportOutcome.DUPLICATE;
		}

		if (transactionRepository.existsByApprovalNumberAndCardAliasAndTerminalIdAndAmount(approval, cardAlias,
				terminal.getId(), amount)) {
			log.warn("Rule 4 duplicate fingerprint skipped: approval={} cardAlias={} terminalId={} amount={}", approval,
					CardAliasMasker.mask(cardAlias), terminal.getId(), amount);
			return ImportOutcome.DUPLICATE;
		}

		CardTransaction tx = new CardTransaction(reference, approval, cardAlias, terminal.getId(), type, productType,
				amount, transactionTime, sourceSystem);
		transactionRepository.save(tx);

		boolean epoch = TransactionService.isEpochDated(transactionTime);
		if (epoch) {
			log.warn("Rule 3 epoch-dated transaction imported: ref={} cardAlias={} time={}", reference,
					CardAliasMasker.mask(cardAlias), transactionTime);
			return ImportOutcome.EPOCH_IMPORTED;
		}
		log.debug("Imported transaction ref={} cardAlias={} line={}", reference, CardAliasMasker.mask(cardAlias),
				lineNo);
		return ImportOutcome.IMPORTED;
	}

	private static String requireText(String raw, String field) {
		if (raw == null || raw.trim().isEmpty()) {
			throw new BusinessException("Bad Request", field + " is required", 400);
		}
		return raw.trim();
	}

	private enum ImportOutcome {
		IMPORTED, DUPLICATE, EPOCH_IMPORTED
	}
}
