package coffeeshout.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import coffeeshout.support.app.IntegrationTestSupport;
import coffeeshout.fixture.UserFixture;
import coffeeshout.user.application.service.AuthTokenService;
import coffeeshout.user.domain.TokenPair;
import coffeeshout.user.domain.User;
import coffeeshout.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
class SecurityConfigTest extends IntegrationTestSupport {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthTokenService authTokenService;

    @Nested
    class admin_체인 {

        @Test
        void admin_엔드포인트는_비로그인_시_로그인_페이지로_리다이렉트된다() throws Exception {
            final MvcResult result = mockMvc.perform(get("/admin"))
                    .andReturn();

            assertThat(result.getResponse().getStatus())
                    .isIn(HttpStatus.FOUND.value(), HttpStatus.MOVED_PERMANENTLY.value());
        }

        @Test
        void admin_login_페이지는_인증_없이_접근_가능하다() throws Exception {
            mockMvc.perform(get("/admin/login"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class admin_api_체인 {

        @Test
        void 토큰_없이_호출하면_401을_반환한다() throws Exception {
            // Thymeleaf 체인이 /admin/api/** 를 먼저 가져가면 로그인 페이지로 리다이렉트(302)된다.
            // 401 이어야 신규 REST 체인(Order 1)이 앞에 있다는 뜻이다.
            mockMvc.perform(get("/admin/api/auth/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void 위변조된_토큰으로_호출하면_401을_반환한다() throws Exception {
            mockMvc.perform(get("/admin/api/auth/me")
                            .header("Authorization", "Bearer invalid.jwt.token"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void 사용자_토큰으로는_관리자_API에_접근할_수_없다() throws Exception {
            // 관리자 secret 이 사용자 JWT secret 으로 폴백될 수 있어, 같은 키로 서명된
            // 사용자 토큰이 관리자로 통과하면 안 된다.
            final User user = userRepository.save(UserFixture.회원_엠제이());
            final TokenPair tokens = authTokenService.issue(user);

            mockMvc.perform(get("/admin/api/auth/me")
                            .header("Authorization", "Bearer " + tokens.accessToken()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void 로그인_엔드포인트는_인증_없이_접근_가능하다() throws Exception {
            // 인증이 아니라 본문 검증에서 걸려야 한다. 401 이면 공개 경로 설정이 빠진 것이다.
            mockMvc.perform(post("/admin/api/auth/login")
                            .contentType("application/json")
                            .content("{\"idToken\":\"\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void dev_login은_test_프로필에_존재하지_않는다() throws Exception {
            // local 프로필에서만 컨트롤러가 등록된다. 배포 환경에 이 경로가 살아 있으면
            // 허용목록에 있는 이메일만으로 구글 검증 없이 관리자 토큰이 나온다.
            mockMvc.perform(post("/admin/api/auth/dev-login")
                            .contentType("application/json")
                            .content("{\"email\":\"mj@zzol.site\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class user_체인_permitAll {

        @Test
        void 게임_API는_인증_없이_접근_가능하다() throws Exception {
            mockMvc.perform(get("/rooms/check-joinCode").param("joinCode", "WXYZ"))
                    .andExpect(status().isOk());
        }

        @Test
        void 건의사항_API는_인증_없이_접근_가능하다() throws Exception {
            mockMvc.perform(post("/reports")
                            .contentType("application/json")
                            .content("{\"category\":\"SUGGESTION\",\"content\":\"테스트\"}"))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    class JWT_인증_필터 {

        @Test
        void 유효한_Bearer_토큰이_있으면_users_me가_200을_반환한다() throws Exception {
            final User user = userRepository.save(UserFixture.회원_엠제이());
            final TokenPair tokens = authTokenService.issue(user);

            mockMvc.perform(get("/users/me")
                            .header("Authorization", "Bearer " + tokens.accessToken()))
                    .andExpect(status().isOk());
        }

        @Test
        void 토큰_없이_users_me를_호출하면_401을_반환한다() throws Exception {
            mockMvc.perform(get("/users/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void 위변조된_토큰으로_users_me를_호출하면_401을_반환한다() throws Exception {
            mockMvc.perform(get("/users/me")
                            .header("Authorization", "Bearer invalid.jwt.token"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
