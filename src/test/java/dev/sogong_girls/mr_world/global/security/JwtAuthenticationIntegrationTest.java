package dev.sogong_girls.mr_world.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import com.jayway.jsonpath.JsonPath;

import dev.sogong_girls.mr_world.domain.user.entity.User;
import dev.sogong_girls.mr_world.domain.booking.service.AdminBookingService;
import dev.sogong_girls.mr_world.domain.user.enums.Role;
import dev.sogong_girls.mr_world.domain.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtAuthenticationIntegrationTest {
    private static final String LOGIN = """
            {"loginId":"tester","password":"password123!"}
            """;

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtTokenProvider tokenProvider;
    @Autowired JwtProperties properties;
    @Autowired TransactionTemplate transactionTemplate;
    @MockitoBean AdminBookingService adminBookingService;

    private Cookie csrfCookie;
    private String csrfHeaderName;
    private String csrfToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        given(adminBookingService.getAllBookingList()).willReturn(List.of());
        MvcResult result = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
        csrfCookie = cookie(result, "XSRF-TOKEN");
        csrfHeaderName = JsonPath.read(result.getResponse().getContentAsString(), "$.headerName");
        csrfToken = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    @Test
    void signupStoresBcryptAndLoginAuthenticatesWithHttpOnlyCookies() throws Exception {
        signup();
        User user = transactionTemplate.execute(status -> userRepository.findByLoginId("tester").orElseThrow());
        assertThat(user.getPassword()).startsWith("$2").isNotEqualTo("password123!");
        assertThat(passwordEncoder.matches("password123!", user.getPassword())).isTrue();
        assertThat(user.getRole()).isEqualTo(Role.USER);

        MvcResult login = login();
        assertThat(login.getResponse().getContentAsString()).isEmpty();
        assertThat(login.getResponse().getHeaders(HttpHeaders.SET_COOKIE)).anySatisfy(header ->
                assertThat(header).contains("access_token=", "HttpOnly", "SameSite=Lax", "Max-Age=1800", "Path=/"))
                .anySatisfy(header -> assertThat(header)
                        .contains("refresh_token=", "HttpOnly", "SameSite=Lax", "Max-Age=1209600", "Path=/api/auth"));
        mvc.perform(get("/api/users/me").cookie(cookie(login, "access_token")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.loginId").value("tester"))
                .andExpect(jsonPath("$.id").value(user.getId().intValue()));
    }

    @Test
    void rejectsBadCredentialsDuplicateIdsAndInvalidInput() throws Exception {
        signup();
        signupRequest().andExpect(status().isConflict());
        mvc.perform(post("/api/auth/login").with(browserCsrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"tester\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").with(browserCsrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"unknown\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").with(browserCsrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshRotatesTokensAndRejectsReuse() throws Exception {
        signup();
        MvcResult login = login();
        Cookie oldRefresh = cookie(login, "refresh_token");
        MvcResult refreshed = mvc.perform(post("/api/auth/refresh").with(browserCsrf()).cookie(oldRefresh,
                new Cookie("access_token", expiredAccessToken())))
                .andExpect(status().isNoContent()).andReturn();
        assertThat(cookie(refreshed, "refresh_token").getValue()).isNotEqualTo(oldRefresh.getValue());
        mvc.perform(get("/api/users/me").cookie(cookie(refreshed, "access_token")))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/refresh").with(browserCsrf()).cookie(oldRefresh))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/refresh").with(browserCsrf()).cookie(cookie(refreshed, "refresh_token")))
                .andExpect(status().isNoContent());
    }

    @Test
    void logoutClearsCookiesAndRevokesRefreshToken() throws Exception {
        signup();
        MvcResult login = login();
        MvcResult logout = mvc.perform(post("/api/auth/logout").with(browserCsrf()).cookie(cookie(login, "refresh_token")))
                .andExpect(status().isNoContent()).andReturn();
        assertThat(cookie(logout, "access_token").getMaxAge()).isZero();
        assertThat(cookie(logout, "refresh_token").getMaxAge()).isZero();
        mvc.perform(post("/api/auth/refresh").with(browserCsrf()).cookie(cookie(login, "refresh_token")))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/logout").with(browserCsrf())).andExpect(status().isNoContent());
    }

    @Test
    void rejectsMissingInvalidExpiredAndRefreshCookiesOnProtectedEndpoints() throws Exception {
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/users/me").cookie(new Cookie("access_token", "invalid")))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/users/me").cookie(new Cookie("access_token", expiredAccessToken())))
                .andExpect(status().isUnauthorized());
        String refresh = tokenProvider.createTokens(1L, Role.USER).refreshToken();
        mvc.perform(get("/api/users/me").cookie(new Cookie("access_token", refresh)))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/refresh").with(browserCsrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void restrictsAdminEndpointsByRole() throws Exception {
        String user = tokenProvider.createTokens(1L, Role.USER).accessToken();
        String admin = tokenProvider.createTokens(1L, Role.ADMIN).accessToken();
        mvc.perform(get("/api/admin/bookings")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/bookings").cookie(new Cookie("access_token", user)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/bookings").cookie(new Cookie("access_token", admin)))
                .andExpect(status().isOk());
    }

    @Test
    void csrfEndpointEnablesBrowserLoginAndMissingCsrfIsRejected() throws Exception {
        signup();
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isForbidden());
        MvcResult csrfResult = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
        String token = JsonPath.read(csrfResult.getResponse().getContentAsString(), "$.token");
        String headerName = JsonPath.read(csrfResult.getResponse().getContentAsString(), "$.headerName");
        mvc.perform(post("/api/auth/login").cookie(cookie(csrfResult, "XSRF-TOKEN"))
                .header(headerName, token).contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isNoContent());
    }

    @Test
    void allowsConfiguredCorsOriginWithCredentials() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/api/auth/login")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type,x-xsrf-token"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Credentials", "true"));
    }

    private void signup() throws Exception {
        signupRequest().andExpect(status().isCreated());
    }

    private org.springframework.test.web.servlet.ResultActions signupRequest() throws Exception {
        return mvc.perform(post("/api/auth/signup").with(browserCsrf()).contentType(MediaType.APPLICATION_JSON).content("""
                {"loginId":"tester","password":"password123!","name":"Tester",
                 "phoneNumber":"01012345678","address":"Seoul"}
                """));
    }

    private MvcResult login() throws Exception {
        return mvc.perform(post("/api/auth/login").with(browserCsrf()).contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isNoContent()).andReturn();
    }

    private Cookie cookie(MvcResult result, String name) {
        Cookie cookie = result.getResponse().getCookie(name);
        assertThat(cookie).isNotNull();
        return cookie;
    }

    private RequestPostProcessor browserCsrf() {
        return request -> {
            var cookies = new ArrayList<Cookie>();
            if (request.getCookies() != null) {
                Collections.addAll(cookies, request.getCookies());
            }
            cookies.add(csrfCookie);
            request.setCookies(cookies.toArray(Cookie[]::new));
            request.addHeader(csrfHeaderName, csrfToken);
            return request;
        };
    }

    private String expiredAccessToken() {
        return Jwts.builder().issuer("mr-world").subject("1").claim("tokenType", "access").claim("role", "USER")
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(properties.secretKey().getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();
    }
}
