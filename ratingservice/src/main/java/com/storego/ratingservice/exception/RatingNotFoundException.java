package com.storego.ratingservice.exception;

/**
 * Excepción personalizada que se lanza cuando se busca una calificación
 * (Rating) que no existe en la base de datos, o que no pertenece al usuario
 * que intenta modificarla/eliminarla.
 *
 * Extiende RuntimeException, así no es obligatorio declararla con "throws"
 * en cada método; el GlobalExceptionHandler la captura automáticamente y la
 * convierte en una respuesta HTTP 404.
 */
public class RatingNotFoundException extends RuntimeException {

    // Constructor simple, solo con el mensaje de error.
    public RatingNotFoundException(String message) {
        super(message);
    }

    // Constructor que además guarda la causa original del error (otra excepción),
    // útil para no perder información de depuración.
    public RatingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}