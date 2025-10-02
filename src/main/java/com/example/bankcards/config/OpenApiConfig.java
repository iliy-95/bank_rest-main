package com.example.bankcards.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  private static final String SECURITY_SCHEME_NAME = "BearerAuth";

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Bank Cards API")
            .version("1.0")
            .description("Документация API для тестирования через Swagger UI"))
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
        .components(new Components()
            .addSecuritySchemes(SECURITY_SCHEME_NAME,
                new SecurityScheme()
                    .name(SECURITY_SCHEME_NAME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")))
        .addTagsItem(new Tag().name("Authentication").description("Эндпоинты доступные всем"))
        .addTagsItem(new Tag().name("User").description("Эндпоинты доступные только пользователям"))
        .addTagsItem(
            new Tag().name("Admin").description("Эндпоинты доступные только администраторам"));

  }


}
