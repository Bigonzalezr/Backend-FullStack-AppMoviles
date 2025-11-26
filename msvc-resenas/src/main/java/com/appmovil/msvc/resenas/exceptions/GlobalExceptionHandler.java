package com.appmovil.msvc.resenas.exceptions;

import com.appmovil.msvc.resenas.dtos.ErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReseñaException.class)
    public ResponseEntity<ErrorDTO> handleReseñaException(ReseñaException ex, HttpServletRequest request) {
        ErrorDTO error = ErrorDTO.builder()
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .errores(Collections.singletonList(ex.getMessage()))
                .build();
        
        HttpStatus status = ex.getMessage().contains("no encontrada") ? 
                HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        ErrorDTO error = ErrorDTO.builder()
                .mensaje("Errores de validación")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .errores(errores)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleException(Exception ex, HttpServletRequest request) {
        ErrorDTO error = ErrorDTO.builder()
                .mensaje("Error interno del servidor")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .errores(Collections.singletonList(ex.getMessage()))
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
