package coffeeshout.admin.auth.domain;

public interface AdminTokenIssuer {

    String issue(String email);

    /**
     * @return 토큰이 유효한 관리자 토큰이면 그 주체
     * @throws coffeeshout.global.exception.custom.BusinessException 만료되었거나 유효하지 않은 경우
     */
    AdminPrincipal verify(String token);
}
