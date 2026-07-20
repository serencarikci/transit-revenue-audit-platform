package com.transit.audit.auditlog.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.transit.audit.auditlog.domain.model.AuditLog;

public interface AuditLogRepository extends Repository<AuditLog, Long> {

	AuditLog save(AuditLog entity);

	Optional<AuditLog> findById(Long id);

	Page<AuditLog> findAll(Pageable pageable);

	@Query("""
			select a from AuditLog a
			where (:entityType is null or a.entityType = :entityType)
			  and (:actor is null or a.actorUsername = :actor)
			order by a.createdAt desc
			""")
	List<AuditLog> search(@Param("entityType") String entityType, @Param("actor") String actor);
}
