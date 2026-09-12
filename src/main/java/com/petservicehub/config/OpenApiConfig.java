package com.petservicehub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI petServiceHubOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("PetServiceHub API")
						.version("1.0.0")
						.description("API para cadastro de tutores, pets, serviços e agendamentos do PetServiceHub")
						.contact(new Contact()
								.name("PetServiceHub")
								.email("contato@petservicehub.com")));
	}
}
