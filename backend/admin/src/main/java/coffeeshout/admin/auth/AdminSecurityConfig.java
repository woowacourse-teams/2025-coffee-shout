package coffeeshout.admin.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

/**
 * 기존 Thymeleaf 백오피스 체인. <b>Phase 5 에서 통째로 제거한다.</b>
 *
 * <p>{@code @Order(2)}로 내린 것은 신규 REST 체인({@link AdminApiSecurityConfig}, Order 1)이
 * {@code /admin/api/**}를 먼저 가져가야 하기 때문이다. 이 체인의 매처 {@code /admin/**}가
 * 그 경로를 포함하므로 순서가 뒤바뀌면 SPA 요청이 폼 로그인 화면으로 리다이렉트된다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(AdminProperties.class)
public class AdminSecurityConfig {

    private final AdminProperties adminProperties;

    @Bean
    @Order(2)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/admin/**")
                // .cors()가 없으면 이 체인이 MVC CORS 설정(web.cors.allowed-origins)을 적용하지 않아
                // 크로스 오리진 어드민 호출(프리플라이트 포함)이 인증 단계에서 막힌다(postmortem 0003).
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth.requestMatchers("/admin/login")
                        .permitAll()
                        .anyRequest()
                        .hasRole("ADMIN"))
                .formLogin(form -> form.loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .defaultSuccessUrl("/admin")
                        .permitAll())
                .logout(logout -> logout.logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout")
                        .permitAll())
                .csrf(csrf -> csrf.ignoringRequestMatchers(new NegatedRequestMatcher(
                        PathPatternRequestMatcher.withDefaults().matcher("/admin/**"))));
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        final UserDetails admin = User.builder()
                .username(adminProperties.username())
                .password(passwordEncoder.encode(adminProperties.password()))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
