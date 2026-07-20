package com.transit.audit.reporting.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.transit.audit.reporting.domain.model.ReportJobStatus;
import com.transit.audit.reporting.domain.model.ReportSnapshot;

public interface ReportSnapshotRepository extends JpaRepository<ReportSnapshot, Long> {

	Optional<ReportSnapshot> findFirstByReportTypeAndParametersJsonAndStatusOrderByCompletedAtDesc(String reportType,
			String parametersJson, ReportJobStatus status);
}
