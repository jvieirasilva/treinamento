package com.api.treinamento.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestão de Fornecedores")
                        .description("API REST para cadastro e gestão de fornecedores brasileiros.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Dev Team")
                                .email("dev@fornecedores.com.br")
                                .url("https://github.com/fornecedor-api"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                	    new Server().url("http://3.223.254.231:8080").description("Servidor AWS EC2"),
                	    new Server().url("http://localhost:8080").description("Servidor local")
                	))
                // 👇 Aplica o esquema globalmente (todos os endpoints terão o cadeado)
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                // 👇 Define o esquema Bearer JWT
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Insere o token JWT. Exemplo: eyJhbGci...")
                        )
                );
    }
}