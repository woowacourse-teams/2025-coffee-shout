package coffeeshout.admin.audit.infra.persistence;

import coffeeshout.admin.audit.domain.AdminAuditLog;
import coffeeshout.admin.audit.domain.AdminAuditLogRepository;
import coffeeshout.admin.audit.domain.AdminAuditResult;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface AdminAuditLogJpaRepository extends Repository<AdminAuditLog, Long>, AdminAuditLogRepository {

    /**
     * 파라미터가 null 이면 그 조건을 건너뛴다.
     *
     * <p>QueryDSL 로 짜지 않았다. 조건이 셋뿐이고 전부 단순 비교라, 동적 쿼리 빌더를
     * 들이면 읽어야 할 코드가 오히려 늘어난다. 조건이 더 붙거나 조인이 생기면 그때 옮긴다.
     *
     * <p>정렬을 쿼리에 박는다. {@code Pageable} 의 정렬에 맡기면 호출하는 쪽이 정렬을
     * 안 주었을 때 순서가 DB 마음대로가 되고, 감사 로그는 순서가 곧 뜻이다.
     */
    @Override
    @Query("""
            SELECT log FROM AdminAuditLog log
            WHERE (:actorEmail IS NULL OR log.actorEmail LIKE CONCAT('%', :actorEmail, '%'))
              AND (:result IS NULL OR log.result = :result)
              AND (:from IS NULL OR log.createdAt >= :from)
            ORDER BY log.createdAt DESC
            """)
    Page<AdminAuditLog> search(
            @Param("actorEmail") String actorEmail,
            @Param("result") AdminAuditResult result,
            @Param("from") Instant from,
            Pageable pageable);
}
