package com.davi.monitor_aiops.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
/**
 * A API utiliza uma API key simples (header) por ser um canal máquina-a-máquina e facilitar
 * integração com agentes externos. O filtro aplica validação apenas em rotas de métricas para
 * manter o restante do backend desacoplado dessa credencial.
 */
public class SecurityConfig {

    // Header utilizado pelo agente para autenticação do endpoint de ingestão.
    private static final String API_KEY_HEADER = "X-API-KEY";

    @Value("${app.security.api-key:obuc-tech-secreta-2026}")
    private String apiKeyValue;

    @Bean
    /**
     * Define a cadeia de filtros de segurança.
     *
     * @param http builder do Spring Security.
     * @return {@link SecurityFilterChain} configurada para autenticação por API key.
     * @throws Exception em erro de construção da cadeia.
     */
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // API stateless; CSRF não se aplica ao cenário de ingestão via cliente não-browser.
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new OncePerRequestFilter() {
                    @Override
                    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                        String path = request.getRequestURI();

                        // Restringe autenticação por API key ao endpoint de ingestão.
                        if (path.startsWith("/api/metrics")) {
                            String apiKey = request.getHeader(API_KEY_HEADER);

                            // Fail-fast: retorna 401 se a credencial não for válida.
                            if (apiKeyValue.equals(apiKey)) {
                                System.out.println("[SECURITY] API key OK para " + request.getMethod() + " " + path);
                                filterChain.doFilter(request, response);
                            } else {
                                System.out.println("[SECURITY] API key inválida para " + request.getMethod() + " " + path + ". Recebido='" + apiKey + "'");
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.getWriter().write("Acesso Negado: API Key ausente ou invalida");
                            }
                        } else {
                            filterChain.doFilter(request, response);
                        }
                    }
                }, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}