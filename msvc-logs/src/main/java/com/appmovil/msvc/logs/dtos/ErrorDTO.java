package com.appmovil.msvc.logs.dtos;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<String> errors;

    public ErrorDTO(Integer status, LocalDateTime timestamp, String message, String path) {
        this.status = status;
        this.timestamp = timestamp;
        this.message = message;
        this.path = path;
    }
}
