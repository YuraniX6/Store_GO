package com.storego.ratingservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de Swagger / OpenAPI para este microservicio.
 *
 * Gracias a esta clase, al entrar a /swagger-ui.html se ve una documentacion
 * interactiva de todos los endpoints (RatingController), con la informacion
 * de titulo, descripcion y, lo mas importante, un boton "Authorize" para
 * poder pegar el token JWT y probar los endpoints protegidos directamente
 * desde el navegador.
 */
@Configuration // Le indica a Spring que esta clase define beans de configuracion.
public class OpenApiConfig {

    // Este metodo crea (y registra como "bean") la configuracion general de OpenAPI.
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // Indica que, por defecto, todos los endpoints requieren el esquema
                // de seguridad "Bearer Authentication" (a menos que se marquen como publicos).
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        // Define el esquema de seguridad "Bearer Authentication":
                        // un token tipo JWT que se envia en el header Authorization
                        // con el formato "Bearer <token>".
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "JWT authentication token. Include the token with 'Bearer ' prefix in the Authorization header.")))
                // Informacion general que se muestra en la parte superior de Swagger UI:
                // titulo, version, descripcion, contacto y licencia.
                .info(new Info()
                        .title("Rating Service API")
                        .version("1.0.0")
                        .description(
                                "Microservicio de calificaciones de skins para StoreGo. Los usuarios se autentican con JWT tokens.")
                        .contact(new Contact()
                                .name("StoreGo Team")
                                .url("https://storego.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
