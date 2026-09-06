package coffeeshout.admin.audit.infra.persistence;

import coffeeshout.admin.audit.domain.AdminAuditLog;
import coffeeshout.admin.audit.domain.AdminAuditLogRepository;
import org.springframework.data.repository.Repository;

public interface AdminAuditLogJpaRepository extends Repository<AdminAuditLog, Long>, AdminAuditLogRepository {}
