package com.appmovil.msvc.resenas.dtos;

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
    private String mensaje;
    private LocalDateTime timestamp;
    private String path;
    private List<String> errores;
}
