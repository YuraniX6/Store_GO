-- Migracion Flyway V1: crea la tabla "ratings" donde se guardan las
-- calificaciones (1 a 5 estrellas) que los usuarios dejan sobre las skins.
-- Flyway ejecuta este script automaticamente al arrancar la aplicacion,
-- la primera vez que detecta que la base de datos esta vacia.

CREATE TABLE ratings (
    -- Identificador unico de la calificacion, generado automaticamente.
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),

    -- ID de la skin calificada (referencia logica a InventoryService).
    skin_id UUID NOT NULL,

    -- ID del usuario que hizo la calificacion (extraido del JWT).
    user_id UUID NOT NULL,

    -- Puntuacion de 1 a 5 (validado tambien por el CHECK de abajo y por @Min/@Max en Java).
    score INTEGER NOT NULL,

    -- Comentario opcional del usuario, maximo 1000 caracteres.
    comment VARCHAR(1000),

    -- Fechas de creacion y ultima actualizacion, manejadas por Hibernate (@CreationTimestamp / @UpdateTimestamp).
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indices para acelerar las busquedas mas comunes: por skin y por usuario.
CREATE INDEX idx_skin_id ON ratings(skin_id);
CREATE INDEX idx_user_id ON ratings(user_id);

-- Restriccion UNICA: un mismo usuario no puede tener mas de una calificacion
-- para la misma skin. Esto es lo que permite el comportamiento de "upsert"
-- en RatingService (si ya existe, se actualiza en vez de duplicar).
ALTER TABLE ratings ADD CONSTRAINT uk_skin_user UNIQUE (skin_id, user_id);

-- Restriccion de validacion a nivel de base de datos: el score SIEMPRE
-- debe estar entre 1 y 5, como ultima linea de defensa (ademas de la
-- validacion que ya hace Java con @Min/@Max en RatingRequest).
ALTER TABLE ratings ADD CONSTRAINT chk_score
    CHECK (score >= 1 AND score <= 5);
