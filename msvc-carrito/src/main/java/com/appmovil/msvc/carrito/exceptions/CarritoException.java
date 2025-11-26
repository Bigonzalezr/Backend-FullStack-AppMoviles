package com.appmovil.msvc.carrito.exceptions;

public class CarritoException extends RuntimeException {
    
    public CarritoException(String message) {
        super(message);
    }
    
    public CarritoException(String message, Throwable cause) {
        super(message, cause);
    }
}
