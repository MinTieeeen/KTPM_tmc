package com.tmc.central.iam.infrastructure;

import com.tmc.central.iam.api.GatePrincipal;
import com.tmc.contract.SyncPaths;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Xác thực Gate Agent trên {@code /sync/**} bằng header {@code X-Gate-Key} (FR-07). Key đúng →
 * {@link GatePrincipal}(gateId) với quyền {@code ROLE_GATE}; thiếu/sai → {@code 401 INVALID_GATE_KEY}.
 * <p>Không khai báo là bean để Spring Boot không tự đăng ký filter cho mọi request; chỉ gắn vào chuỗi
 * {@code /sync/**} trong {@link SecurityConfig}.
 */
public class GateApiKeyFilter extends OncePerRequestFilter {

    static final String ROLE_GATE = "ROLE_GATE";

    private final GateApiKeyRegistry registry;
    private final ProblemSecurityHandlers handlers;

    public GateApiKeyFilter(GateApiKeyRegistry registry, ProblemSecurityHandlers handlers) {
        this.registry = registry;
        this.handlers = handlers;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Optional<String> gateId = registry.resolve(request.getHeader(SyncPaths.GATE_KEY_HEADER));
        if (gateId.isEmpty()) {
            SecurityContextHolder.clearContext();
            handlers.commenceGate(request, response);
            return;
        }
        UsernamePasswordAuthenticationToken auth = UsernamePasswordAuthenticationToken.authenticated(
                new GatePrincipal(gateId.get()), null, List.of(new SimpleGrantedAuthority(ROLE_GATE)));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        chain.doFilter(request, response);
    }
}
