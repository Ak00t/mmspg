package com.ojt_22.mmspg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		final String bearerAuthScheme = "bearerAuth";
		final String clientIdScheme = "xClientId";
		final String clientSecretScheme = "xClientSecret";

		return new OpenAPI().info(new Info().title("mmspg API")
				.version("1.0")
				.description("API documentation for mmspg project"))
				// Global အနေဖြင့် Swagger UI မှ ခေါ်ဆိုမှုတိုင်းတွင် ပါဝင်စေရန် သတ်မှတ်ခြင်း
				.addSecurityItem(new SecurityRequirement().addList(bearerAuthScheme))
				.addSecurityItem(new SecurityRequirement().addList(clientIdScheme)
						.addList(clientSecretScheme))
				.components(new Components()
						// 1. JWT Bearer Token Scheme
						.addSecuritySchemes(bearerAuthScheme, new SecurityScheme().name(bearerAuthScheme)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT"))
						// 2. X-Client-ID Header Scheme
						.addSecuritySchemes(clientIdScheme, new SecurityScheme().name("X-Client-ID")
								.type(SecurityScheme.Type.APIKEY)
								.in(SecurityScheme.In.HEADER)
								.description("Merchant Client ID"))
						// 3. X-Client-Secret Header Scheme
						.addSecuritySchemes(clientSecretScheme, new SecurityScheme().name("X-Client-Secret")
								.type(SecurityScheme.Type.APIKEY)
								.in(SecurityScheme.In.HEADER)
								.description("Merchant Client Secret")));
	}
}