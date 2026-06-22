CREATE TABLE cart_items (
    id              BIGSERIAL      NOT NULL,
    user_id         BIGINT         NOT NULL,
    publication_id  BIGINT         NOT NULL,
    quantity        INTEGER        NOT NULL,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_cart_items PRIMARY KEY (id)
);

CREATE INDEX idx_cart_user_id
ON cart_items(user_id);

CREATE INDEX idx_cart_publication_id
ON cart_items(publication_id);