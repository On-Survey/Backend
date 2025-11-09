package OneQ.OnSurvey.global.config;

import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.global.auth.filter.ExactBasicHeaderFilter;
import OneQ.OnSurvey.global.auth.filter.TossAuthFilter;
import OneQ.OnSurvey.global.handler.CustomAccessDeniedHandler;
import OneQ.OnSurvey.global.handler.JWTAuthenticationEntryPoint;
import OneQ.OnSurvey.global.infra.toss.auth.service.TossAuthService;
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${toss.basic.header}")
    private String expectedHeader;

    private final JWTAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final MemberRepository memberRepository;
    private final TossAuthService tossAuthService;

    private final String[] allowedUrls = {
            "/",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/health",
            "/auth/toss/login",
            "/auth/reissue",
            "/connect-out",
            "/test/ok"
    };

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TossAuthFilter tossAuthFilter() {
        return new TossAuthFilter(tossAuthService, memberRepository, jwtAuthenticationEntryPoint);
    }

    @Bean @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http, TossAuthFilter tossAuthFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(allowedUrls).permitAll()
                        .anyRequest().hasRole("MEMBER")
                )
                .addFilterBefore(tossAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler));
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

