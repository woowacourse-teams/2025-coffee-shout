package coffeeshout.admin.audit.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminAuditLogRepository {

    AdminAuditLog save(AdminAuditLog auditLog);

    /** 최근 조치부터. 감사 로그는 언제나 "방금 무슨 일이 있었나"로 읽는다. */
    Page<AdminAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
