package coffeeshout.admin.audit.domain;

public interface AdminAuditLogRepository {

    AdminAuditLog save(AdminAuditLog auditLog);
}
