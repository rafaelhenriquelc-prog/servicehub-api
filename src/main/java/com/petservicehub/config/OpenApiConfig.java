package com.petservicehub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI petServiceHubOpenAPI() {
		return new OpenAPI()
				.components(new Components().addSecuritySchemes("bearer-jwt", new SecurityScheme()
						.name("bearer-jwt")
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")
						.description("Informe o token JWT obtido em POST /api/auth/login")))
				.info(new Info()
						.title("ServiceHub API")
						.version("1.0.0")
						.description("API da plataforma ServiceHub: clientes localizam prestadores, contratam serviços e registram avaliações")
						.contact(new Contact()
								.name("ServiceHub")
								.email("contato@petservicehub.com")));
	}
}
