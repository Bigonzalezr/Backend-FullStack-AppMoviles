package com.appmovil.msvc.logs.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarLogDTO {
    
    private Long idUsuario;
    
    @NotBlank(message = "El tipo de actividad no puede estar vacío")
    private String tipoActividad;
    
    private String descripcion;
    private String ipAddress;
    private String userAgent;
    private Long idRecurso;
    private String tipoRecurso;
    private String resultado;
    private String datosAdicionales;
}
