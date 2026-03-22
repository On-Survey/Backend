package OneQ.OnSurvey.global.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .servers(List.of(
                new Server().url("https://api.onsurvey.co.kr").description("Prod Server"),
                new Server().url("https://dev.api.onsurvey.co.kr").description("Dev Server"),
                new Server().url("http://localhost:8080").description("Local Server")
            ))
            .components(components())
            .info(apiInfo())
            .addSecurityItem(securityRequirement());
    }

    private Info apiInfo() {
        return new Info()
            .title("OnSurvey API Docs")
            .description("OnSurvey Spring boot Api Document 입니다.");
    }

    private Components components() {
        String securityScheme = "jwtToken";
        String basicScheme = "basicAuth";
        return new Components()
            .addSecuritySchemes(securityScheme,
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            )
            .addSecuritySchemes(basicScheme,
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("basic")
            );
    }

    private SecurityRequirement securityRequirement() {
        String securityScheme = "jwtToken";
        return new SecurityRequirement().addList(securityScheme);
    }
}

