package com.appmovil.msvc.pagos.exceptions;

public class PagoException extends RuntimeException {
    public PagoException(String message) {
        super(message);
    }
    
    public PagoException(String message, Throwable cause) {
        super(message, cause);
    }
}
