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
public class SecurityConfig {

    // O nome do cabeçalho que o Python terá que enviar
    private static final String API_KEY_HEADER = "X-API-KEY";

    @Value("${app.security.api-key:obuc-tech-secreta-2026}")
    private String apiKeyValue;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // Desabilita proteção de formulários web (nosso foco é só backend/API)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new OncePerRequestFilter() {
                    @Override
                    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                        String path = request.getRequestURI();

                        // Verifica se a rota que estão tentando acessar é a de métricas
                        if (path.startsWith("/api/metrics")) {
                            String apiKey = request.getHeader(API_KEY_HEADER);

                            // Confere se a chave enviada bate com a nossa chave secreta
                            if (apiKeyValue.equals(apiKey)) {
                                // Chave correta! O guarda-costas deixa passar.
                                System.out.println("[SECURITY] API key OK para " + request.getMethod() + " " + path);
                                filterChain.doFilter(request, response);
                            } else {
                                // Sem chave ou chave errada! Bloqueia com Erro 401.
                                System.out.println("[SECURITY] API key inválida para " + request.getMethod() + " " + path + ". Recebido='" + apiKey + "'");
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.getWriter().write("Acesso Negado: API Key ausente ou invalida");
                            }
                        } else {
                            // Se for outra rota do sistema, deixa passar
                            filterChain.doFilter(request, response);
                        }
                    }
                }, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}