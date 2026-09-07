package coffeeshout.admin.auth;

import coffeeshout.admin.auth.domain.AdminTokenIssuer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 신규 백오피스 SPA(admin-web)가 쓰는 REST 체인.
 *
 * <p>한때 Thymeleaf 백오피스 체인이 {@code @Order(2)}로 함께 있었다. 신규 SPA 로 넘어오면서
 * 걷어냈고, 이제 관리자 경로를 지키는 체인은 이것 하나다.
 *
 * <p>필터 체인 순서: 0 = ws-catalog·internal webhook, <b>1 = admin API</b>,
 * 3 = user(매처 없는 fallback). 2번은 비어 있다 - 번호를 당기지 않는 이유는,
 * 순서 값이 곧 이 파일들 사이의 약속이라 다시 매기면 전부 고쳐야 하기 때문이다.
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AdminAuthProperties.class)
public class AdminApiSecurityConfig {

    private static final String LOGIN_PATH = "/admin/api/auth/login";
    private static final String DEV_LOGIN_PATH = "/admin/api/auth/dev-login";
    private static final String LOCAL_PROFILE = "local";

    private final AdminTokenIssuer adminTokenIssuer;
    private final Environment environment;

    @Bean
    @Order(1)
    public SecurityFilterChain adminApiFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/admin/api/**")
                // 없으면 admin-web(다른 오리진)의 프리플라이트가 인증 단계에서 막힌다(postmortem 0003).
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth.requestMatchers(publicPaths())
                        .permitAll()
                        .anyRequest()
                        .hasRole("ADMIN"))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // formLogin·httpBasic 을 끄면 인증 진입점이 사라져 스프링이 기본값인
                // Http403ForbiddenEntryPoint 를 쓴다. 그러면 미인증도 403 이 되어
                // SPA 가 "토큰 재발급하면 되는 상황"과 "권한이 없어 소용없는 상황"을 구분하지 못한다.
                .exceptionHandling(handling ->
                        handling.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // 토큰 인증이라 세션 쿠키가 없다. CSRF 는 쿠키 자동 전송이 있을 때의 문제다.
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .addFilterBefore(
                        new AdminJwtAuthenticationFilter(adminTokenIssuer), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * dev-login 은 local 프로필에서만 공개한다. 컨트롤러의 {@code @Profile("local")}과 합쳐
     * 두 겹으로 잠근다. 배포 환경에는 엔드포인트도 없고 공개 경로도 아니다.
     */
    private String[] publicPaths() {
        if (environment.matchesProfiles(LOCAL_PROFILE)) {
            return new String[] {LOGIN_PATH, DEV_LOGIN_PATH};
        }
        return new String[] {LOGIN_PATH};
    }
}
