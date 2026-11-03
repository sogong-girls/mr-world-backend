package dev.sogong_girls.mr_world.global.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import dev.sogong_girls.mr_world.global.security.JwtTokenProvider.JwtPrincipal;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Cookie cookie = WebUtils.getCookie(request, "access_token");
        if (cookie != null) {
            try {
                JwtPrincipal principal = tokenProvider.parseAccessToken(cookie.getValue());
                var authentication = UsernamePasswordAuthenticationToken.authenticated(principal, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + principal.role().name())));
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
            }
        }
        // Invalid/expired access cookies must not block login or refresh requests.
        filterChain.doFilter(request, response);
    }
}
