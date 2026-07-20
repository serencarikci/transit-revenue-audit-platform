package com.transit.audit.transaction.web.response;

import java.util.List;

public record TransactionImportResult(int totalRows, int importedCount, int skippedDuplicateCount, int epochDatedCount,
		List<String> errors) {
}
