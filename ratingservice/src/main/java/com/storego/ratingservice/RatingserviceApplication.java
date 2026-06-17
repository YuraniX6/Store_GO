package com.storego.ratingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del microservicio: el punto de entrada del programa.
 *
 * @SpringBootApplication es una anotación "combo" que activa, entre otras
 * cosas: el autoconfigurado de Spring Boot, el escaneo de componentes
 * (@Service, @RestController, @Repository, etc.) dentro de este paquete y
 * sus subpaquetes, y la configuración de Spring basada en application.yml.
 */
@SpringBootApplication
public class RatingserviceApplication {

	// Método main: lo que se ejecuta al arrancar la aplicación (por ejemplo,
	// con "mvn spring-boot:run" o al darle Run en VS Code).
	public static void main(String[] args) {
		SpringApplication.run(RatingserviceApplication.class, args);
	}

}
