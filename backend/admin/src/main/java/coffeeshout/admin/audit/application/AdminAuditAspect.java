package coffeeshout.admin.audit.application;

import coffeeshout.admin.audit.domain.AdminAuditResult;
import coffeeshout.admin.auth.domain.AdminPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

/**
 * {@code /admin/api/**}의 상태를 바꾸는 요청을 감사 로그에 남긴다.
 *
 * <p>컨트롤러마다 기록 코드를 넣지 않는다. 사람이 매번 기억해야 하는 규칙은 언젠가 빠지고,
 * 빠진 것을 알아차리는 시점은 대개 "누가 이걸 했는지 확인해야 하는" 바로 그때다.
 *
 * <p>조회(GET)는 남기지 않는다. 목록을 열어본 기록까지 쌓으면 실제 조치가 묻힌다.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AdminAuditAspect {

    private static final String ADMIN_API_PREFIX = "/admin/api/";
    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final String ANONYMOUS = "anonymous";

    private final AdminAuditLogService adminAuditLogService;

    @Around("@within(org.springframework.web.bind.annotation.RestController) && within(coffeeshout.admin..*)")
    public Object recordWrite(ProceedingJoinPoint joinPoint) throws Throwable {
        final HttpServletRequest request = currentRequest();
        if (request == null || !isAuditTarget(request)) {
            return joinPoint.proceed();
        }

        final String actor = currentActorEmail();
        final String action = request.getMethod() + " " + uriTemplate(request);
        final String targetId = pathVariablesOf(request);

        try {
            final Object result = joinPoint.proceed();
            adminAuditLogService.record(actor, action, targetTypeOf(request), targetId, null, AdminAuditResult.SUCCESS);
            return result;
        } catch (Throwable e) {
            // 실패도 남긴다. 반복된 실패가 곧 신호다.
            adminAuditLogService.record(
                    actor,
                    action,
                    targetTypeOf(request),
                    targetId,
                    e.getClass().getSimpleName() + ": " + e.getMessage(),
                    AdminAuditResult.FAILURE);
            throw e;
        }
    }

    private static HttpServletRequest currentRequest() {
        final var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            return servletAttributes.getRequest();
        }
        return null;
    }

    private static boolean isAuditTarget(HttpServletRequest request) {
        return request.getRequestURI().startsWith(ADMIN_API_PREFIX) && WRITE_METHODS.contains(request.getMethod());
    }

    private static String currentActorEmail() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AdminPrincipal admin) {
            return admin.email().value();
        }
        // 로그인 시도는 인증 전이라 주체가 없다. 그래도 남긴다. 실패한 로그인 반복이 신호다.
        return ANONYMOUS;
    }

    /**
     * 실제 URI 대신 매핑 패턴({@code /admin/api/accounts/{id}})을 쓴다.
     * id 마다 다른 action 이 쌓이면 "무슨 조치가 몇 번 있었나"를 집계할 수 없다.
     */
    private static String uriTemplate(HttpServletRequest request) {
        final Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return pattern != null ? pattern.toString() : request.getRequestURI();
    }

    private static String targetTypeOf(HttpServletRequest request) {
        // /admin/api/{targetType}/... 의 첫 세그먼트를 자원 이름으로 본다.
        final String path = request.getRequestURI().substring(ADMIN_API_PREFIX.length());
        final int slash = path.indexOf('/');
        final String first = slash < 0 ? path : path.substring(0, slash);
        return first.isBlank() ? null : first;
    }

    @SuppressWarnings("unchecked")
    private static String pathVariablesOf(HttpServletRequest request) {
        final Object variables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (variables instanceof java.util.Map<?, ?> map && !map.isEmpty()) {
            return map.values().stream()
                    .map(String::valueOf)
                    .reduce((a, b) -> a + "," + b)
                    .orElse(null);
        }
        return null;
    }
}
