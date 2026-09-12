package coffeeshout.admin.audit.ui;

import coffeeshout.admin.audit.domain.AdminAuditLogRepository;
import coffeeshout.admin.audit.domain.AdminAuditResult;
import coffeeshout.admin.audit.ui.response.AdminAuditLogResponse;
import coffeeshout.admin.support.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 조치 이력 조회.
 *
 * <p>Grafana 로는 절대 볼 수 없는 정보다. 누가 언제 어떤 IP 를 풀었고 어떤 닉네임을
 * 허용했는지는 우리 DB 에만 있다. 조회만 있고 쓰기는 없다. 감사 로그는 AOP 가 자동으로
 * 남기고 사람은 읽기만 한다.
 */
@RestController
@RequestMapping("/admin/api/audit-logs")
@Validated
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AdminAuditLogRepository adminAuditLogRepository;
    private final Clock clock;

    /**
     * @param size       홈 화면은 5건만 필요하고 전체 화면은 더 본다. 상한을 둬서 한 번에
     *                   수천 건을 끌어오는 것을 막는다
     * @param actorEmail 부분 일치. 비우면 전체
     * @param result     SUCCESS 또는 FAILURE. 비우면 전체
     * @param days       최근 며칠. 비우면 기간 제한 없음. 상한 365 는 이 표가 전 기간을
     *                   훑는 자리가 아니기 때문이다 - 그만큼 거슬러 올라갈 일이면 SQL 로 간다
     */
    @GetMapping
    @Transactional(readOnly = true)
    public PageResponse<AdminAuditLogResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String actorEmail,
            @RequestParam(required = false) AdminAuditResult result,
            @RequestParam(required = false) @Min(1) @Max(365) Integer days) {
        return PageResponse.of(
                adminAuditLogRepository.search(
                        blankToNull(actorEmail), result, since(days), PageRequest.of(page, size)),
                AdminAuditLogResponse::from);
    }

    /**
     * 빈 문자열을 null 로 바꾼다.
     *
     * <p>화면의 검색창을 비우면 {@code actorEmail=} 로 온다. 그대로 넘기면 빈 문자열
     * LIKE 가 되어 조건이 걸린 것도 안 걸린 것도 아닌 상태가 된다.
     */
    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private Instant since(Integer days) {
        return days == null ? null : clock.instant().minus(Duration.ofDays(days));
    }
}
