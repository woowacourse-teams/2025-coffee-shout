package coffeeshout.admin.audit.ui;

import coffeeshout.admin.audit.domain.AdminAuditLogRepository;
import coffeeshout.admin.audit.ui.response.AdminAuditLogResponse;
import coffeeshout.admin.support.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    /**
     * @param size 홈 화면은 5건만 필요하고 전체 화면은 더 본다. 상한을 둬서 한 번에
     *             수천 건을 끌어오는 것을 막는다.
     */
    @GetMapping
    @Transactional(readOnly = true)
    public PageResponse<AdminAuditLogResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return PageResponse.of(
                adminAuditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)),
                AdminAuditLogResponse::from);
    }
}
