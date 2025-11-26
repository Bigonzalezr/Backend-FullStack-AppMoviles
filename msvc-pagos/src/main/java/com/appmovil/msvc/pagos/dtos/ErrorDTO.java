package com.appmovil.msvc.pagos.dtos;

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
    private String mensaje;
    private LocalDateTime timestamp;
    private String path;
    private Map<String, String> errores;
}
