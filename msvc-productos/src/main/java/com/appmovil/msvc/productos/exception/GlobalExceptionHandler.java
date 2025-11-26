package com.appmovil.msvc.productos.exception;

import com.appmovil.msvc.productos.dtos.ErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductoException.class)
    public ResponseEntity<ErrorDTO> handleProductoException(ProductoException ex, HttpServletRequest request) {
        HttpStatus status = ex.getMessage().contains("no encontrado") || ex.getMessage().contains("no existe") 
                ? HttpStatus.NOT_FOUND 
                : HttpStatus.BAD_REQUEST;
        
        ErrorDTO error = ErrorDTO.builder()
                .status(status.value())
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errores = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .mensaje("Error de validación")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .errores(errores)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDTO);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleGenericException(Exception ex, HttpServletRequest request) {
        ErrorDTO error = ErrorDTO.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .mensaje("Error interno del servidor: " + ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}