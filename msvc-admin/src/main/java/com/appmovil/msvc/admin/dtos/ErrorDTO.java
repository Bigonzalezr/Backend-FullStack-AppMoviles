package com.appmovil.msvc.admin.dtos;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorDTO {
    
    private Integer status;
    
    private LocalDateTime timestamp;
    
    private String message;
    
    private String path;
    
    private Map<String, String> errors;
    
    public ErrorDTO(Integer status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorDTO(Integer status, String message, Map<String, String> errors) {
        this.status = status;
        this.message = message;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }
}
