package OneQ.OnSurvey.global.common.config;

import OneQ.OnSurvey.global.auth.application.strategy.AuthStrategy;
import OneQ.OnSurvey.global.auth.filter.AuthFilter;
import OneQ.OnSurvey.global.auth.filter.BOSessionFilter;
import OneQ.OnSurvey.global.auth.filter.ExactBasicHeaderFilter;
import OneQ.OnSurvey.global.common.handler.CustomAccessDeniedHandler;
import OneQ.OnSurvey.global.common.handler.JWTAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${toss.basic.header}")
    private String expectedHeader;

    private final AuthStrategy authStrategy;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final JWTAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    private final String[] allowedUrls = {
            "/",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/health",
            "/auth/toss/login",
            "/auth/reissue",
            "/connect-out",
            "/toss/promotion/recheck-pending"
    };

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean @Order(3)
    public SecurityFilterChain filterChain(HttpSecurity http, BOSessionFilter boFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(allowedUrls).permitAll()
                        .anyRequest().hasAnyRole("MEMBER", "ADMIN")
                )
                .addFilterBefore(new AuthFilter(authStrategy, authenticationEntryPoint), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(boFilter, AuthFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler));
        return http.build();
    }

    @Bean @Order(2)
    public SecurityFilterChain boFilterChain(HttpSecurity http, BOSessionFilter boFilter) throws Exception {
        http
            .securityMatcher("/v1/bo/**", "bo/**")
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/v1/bo", "/v1/bo/admin/login").permitAll()
                .anyRequest().hasRole("ADMIN")
            )
            .headers(headers -> headers.frameOptions(
                HeadersConfigurer.FrameOptionsConfig::sameOrigin
            ))
            .addFilterBefore(boFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean @Order(1)
    public SecurityFilterChain tossUnlinkChain(HttpSecurity http)
            throws Exception {
        http
                .securityMatcher("/connect-out")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().permitAll())
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(new ExactBasicHeaderFilter(expectedHeader), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler));
        return http.build();
    }
}

