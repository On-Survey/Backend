package OneQ.OnSurvey.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173", //Todo 배포시 삭제할 것들 확인
                "http://localhost:8080",
                "https://api.onsurvey.co.kr",
                "https://onsurvey.private-apps.tossmini.com",
                "https://onsurvey.apps.tossmini.com"
        ));
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH" ,"OPTIONS"
        ));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(Arrays.asList(
                "Authorization", "X-Refresh-Token"
        ));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

