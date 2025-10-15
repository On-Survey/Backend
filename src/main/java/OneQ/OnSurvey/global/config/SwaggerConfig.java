package OneQ.OnSurvey.global.config;

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
        String securityScheme = "JWT TOKEN";
        String basicScheme = "BASIC AUTH";
        return new Components()
                .addSecuritySchemes(securityScheme, new SecurityScheme()
                        .name(securityScheme)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("Bearer")
                        .bearerFormat("JWT")
                )
                .addSecuritySchemes(basicScheme,
                        new SecurityScheme()
                                .name(basicScheme)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                );
    }

    private SecurityRequirement securityRequirement() {
        String securityScheme = "JWT TOKEN";
        return new SecurityRequirement().addList(securityScheme);
    }
}

