package aded.first_web_api.doc;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;


@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .servers(List.of(new Server().url("/")))
            .info(new Info()
                .title("Resume Review API with Spring Boot")
                .description("API developed for resume reviewing using OpenAI.")
                .version("1.0")
                .termsOfService("Terms of service: Only for learning purposes.")
                .contact(new Contact()
                    .name("João Pedro Aded")
                    .url("https://github.com/jpadedg")
                    .email("joaopedro.aded@gmail.com")
                )
            );
    }
}
