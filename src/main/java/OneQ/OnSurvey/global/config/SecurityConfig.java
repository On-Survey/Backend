package OneQ.OnSurvey.global.config;

import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.global.auth.filter.JWTFilter;
import OneQ.OnSurvey.global.auth.token.service.BlackListService;
import OneQ.OnSurvey.global.auth.utils.JWTUtil;
import OneQ.OnSurvey.global.handler.CustomAccessDeniedHandler;
import OneQ.OnSurvey.global.handler.JWTAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Value("${spring.jwt.iss}")
    private String tokenIss;

    @Value("${spring.token.access.category}")
    private String accessTokenCategory;

    @Value("${toss.basic.header}")
    private String expectedHeader;

    private final JWTUtil jwtUtil;
    private final JWTAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final BlackListService blackListService;
    private final MemberRepository memberRepository;

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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(allowedUrls).permitAll()
                        .anyRequest().hasRole("MEMBER")
                )
                .addFilterBefore(new JWTFilter(tokenIss, jwtUtil, jwtAuthenticationEntryPoint, blackListService, memberRepository, accessTokenCategory), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler));
        return http.build();
    }
}

