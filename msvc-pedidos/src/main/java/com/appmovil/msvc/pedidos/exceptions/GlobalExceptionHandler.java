package com.appmovil.msvc.pedidos.exceptions;

import com.appmovil.msvc.pedidos.dtos.ErrorDTO;
import com.appmovil.msvc.pedidos.exceptions.PedidoException;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ErrorDTO createErrorDTO(int status, Date date, Map<String, String> errorMap) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setStatus(status);
        errorDTO.setDate(date);
        errorDTO.setErrors(errorMap);
        return errorDTO;
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleValidationFields(MethodArgumentNotValidException exception){
        Map<String, String> errorMap = new HashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(this.createErrorDTO(HttpStatus.BAD_REQUEST.value(), new Date(), errorMap));
    }


    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ErrorDTO> handleFeignNotFound(FeignException.NotFound exception) {
        Map<String, String> errorMap = Collections.singletonMap("Recurso Externo", "No se encontró el recurso externo necesario para el pedido (Usuario o Producto).");
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(this.createErrorDTO(HttpStatus.NOT_FOUND.value(), new Date(), errorMap));
    }


    @ExceptionHandler(PedidoException.class)
    public ResponseEntity<ErrorDTO> handlePedidoException(PedidoException exception){

        if(exception.getMessage().contains("no encontrado") || exception.getMessage().contains("no existe")) {

            Map<String, String> errorMap = Collections.singletonMap("Pedido", exception.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(this.createErrorDTO(HttpStatus.NOT_FOUND.value(), new Date(), errorMap));
        } else if (exception.getMessage().contains("Stock insuficiente")) {

            Map<String, String> errorMap = Collections.singletonMap("Inventario", exception.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(this.createErrorDTO(HttpStatus.CONFLICT.value(), new Date(), errorMap));
        } else {

            Map<String, String> errorMap = Collections.singletonMap("Error de Pedido", exception.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(this.createErrorDTO(HttpStatus.BAD_REQUEST.value(), new Date(), errorMap));
        }
    }
}