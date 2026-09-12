package com.ojt_22.mmspg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement; // 🔴 ဤ Import ကို ထပ်ထည့်ရပါမည်
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		final String securitySchemeName = "bearerAuth";

		return new OpenAPI()
				.info(new Info().title("mmspg API").version("1.0").description("API documentation for mmspg project"))
				// 🔴 Swagger မှ API များခေါ်တိုင်း Token ကို Header တွင် ထည့်ပေးရန် ဤစာကြောင်းကို မဖြစ်မနေ ပြန်ထည့်ရပါမည်
				.addSecurityItem(new SecurityRequirement().addList(securitySchemeName)) 
				.components(new Components().addSecuritySchemes(securitySchemeName, new SecurityScheme()
						.name(securitySchemeName)
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")));
	}
}