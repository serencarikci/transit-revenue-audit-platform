package com.transit.audit.anomaly.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transit.audit.anomaly.domain.model.Anomaly;
import com.transit.audit.anomaly.domain.model.AnomalySeverity;
import com.transit.audit.anomaly.domain.model.AnomalyStatus;

public interface AnomalyRepository extends JpaRepository<Anomaly, Long> {

	@Query("""
			select a from Anomaly a
			where (:severity is null or a.severity = :severity)
			  and (:status is null or a.status = :status)
			  and (:entityType is null or a.entityType = :entityType)
			order by a.detectedAt desc
			""")
	List<Anomaly> search(@Param("severity") AnomalySeverity severity, @Param("status") AnomalyStatus status,
			@Param("entityType") String entityType);

	boolean existsByRuleCodeAndEntityTypeAndEntityIdAndStatus(String ruleCode, String entityType, String entityId,
			AnomalyStatus status);
}
