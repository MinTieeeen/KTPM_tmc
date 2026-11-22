package com.tmc.central.iam.infrastructure;

import com.tmc.central.iam.application.IamProperties;
import com.tmc.central.iam.domain.UserAccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Hai chuỗi bộ lọc (ma trận quyền mục 5.2):
 * <ol>
 *   <li>{@code /sync/**}: chỉ {@code X-Gate-Key} ({@link GateApiKeyFilter}); JWT không được chấp nhận.</li>
 *   <li>Còn lại: OAuth2 Resource Server JWT RS256 + {@link JwtRoleConverter}; một số đường dẫn công khai.</li>
 * </ol>
 * Stateless, CSRF tắt (token trong header – NFR-04). {@code @PreAuthorize} trên controller kiểm vai trò lần nữa.
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    JwtDecoder jwtDecoder(JwtKeyProvider keys, IamProperties props) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(keys.publicKey()).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(props.jwt().issuer()));
        return decoder;
    }

    @Bean
    @Order(1)
    SecurityFilterChain syncFilterChain(HttpSecurity http, GateApiKeyRegistry registry,
                                        ProblemSecurityHandlers handlers) throws Exception {
        http.securityMatcher("/sync/**");
        stateless(http);
        http.addFilterBefore(new GateApiKeyFilter(registry, handlers), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("GATE"))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> handlers.commenceGate(req, res))
                        .accessDeniedHandler(handlers));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain apiFilterChain(HttpSecurity http, JwtDecoder jwtDecoder, UserAccountRepository accounts,
                                       ProblemSecurityHandlers handlers) throws Exception {
        JwtRoleConverter roleConverter = new JwtRoleConverter(accounts);
        stateless(http);
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers("/", "/*.html", "/assets/**", "/favicon.ico", "/error",
                                "/actuator/health", "/actuator/health/**",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/portal/**").hasRole("EMPLOYEE")
                        .requestMatchers("/employees", "/employees/**", "/stations", "/stations/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(rs -> rs
                        .jwt(jwt -> jwt.decoder(jwtDecoder).jwtAuthenticationConverter(roleConverter))
                        .authenticationEntryPoint(handlers)
                        .accessDeniedHandler(handlers))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(handlers).accessDeniedHandler(handlers));
        return http.build();
    }

    private static void stateless(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }
}
