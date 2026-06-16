package com.storego.catalog.exception;

public class InvalidCatalogSkinJsonException extends RuntimeException {

    public InvalidCatalogSkinJsonException(String reason) {
        super(reason);
    }
}
