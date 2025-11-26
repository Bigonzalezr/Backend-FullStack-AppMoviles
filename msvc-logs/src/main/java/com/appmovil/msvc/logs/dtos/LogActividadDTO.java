package com.appmovil.msvc.logs.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogActividadDTO {
    private Long idLog;
    private Long idUsuario;
    private String usernameUsuario;
    private String tipoActividad;
    private LocalDateTime fecha;
    private String descripcion;
    private String ipAddress;
    private String userAgent;
    private Long idRecurso;
    private String tipoRecurso;
    private String resultado;
    private String datosAdicionales;
}
