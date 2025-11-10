package com.tickets.backend.config;

import com.tickets.backend.dto.auth.TokenPayload;
import com.tickets.backend.service.AuthTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class AuthTokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthTokenService tokenService;

    public AuthTokenAuthenticationFilter(AuthTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Optional<String> tokenOptional = resolveBearerToken(request);
            if (tokenOptional.isPresent()) {
                TokenPayload payload = tokenService.parseToken(tokenOptional.get());
                List<SimpleGrantedAuthority> authorities = payload.roles().stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(payload.email(), null, authorities);
                authentication.setDetails(payload);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException ex) {
            response.reset();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"invalid_token\",\"message\":\"" + ex.getMessage() + "\"}");
        }
    }

    private Optional<String> resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return Optional.empty();
        }
        return Optional.of(header.substring(7));
    }
}
