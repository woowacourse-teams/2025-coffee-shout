package coffeeshout.admin.audit.application;

import coffeeshout.admin.audit.domain.AdminAuditLog;
import coffeeshout.admin.audit.domain.AdminAuditLogRepository;
import coffeeshout.admin.audit.domain.AdminAuditResult;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuditLogService {

    private final AdminAuditLogRepository adminAuditLogRepository;
    private final Clock clock;

    /**
     * 감사 기록을 남긴다.
     *
     * <p>{@code REQUIRES_NEW}로 별도 트랜잭션에서 커밋한다. 조치가 실패해 업무 트랜잭션이
     * 롤백되면 같은 트랜잭션에 있던 감사 기록도 함께 사라진다. 실패한 시도야말로 남아야 하므로
     * 트랜잭션을 분리한다.
     *
     * <p>기록 자체가 실패해도 예외를 밖으로 던지지 않는다. 감사 로그가 원래 요청을 죽이면
     * 백오피스 전체가 감사 테이블 가용성에 묶인다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(
            String actorEmail,
            String action,
            String targetType,
            String targetId,
            String detail,
            AdminAuditResult result
    ) {
        try {
            adminAuditLogRepository.save(AdminAuditLog.of(
                    actorEmail, action, targetType, targetId, detail, result, clock.instant()));
        } catch (Exception e) {
            log.error("관리자 감사 로그 기록 실패: actor={} action={} result={}",
                    actorEmail, action, result, e);
        }
    }
}
