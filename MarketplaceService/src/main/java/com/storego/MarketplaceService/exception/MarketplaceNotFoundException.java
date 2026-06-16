package com.storego.MarketplaceService.exception;

public class MarketplaceNotFoundException extends RuntimeException {
    
    public MarketplaceNotFoundException(String message) {
        super(message);
    }
    
    public MarketplaceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
