package com.appmovil.msvc.resenas.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReseñaDTO {
    private Long id;
    private Long idUsuario;
    private String nombreUsuario;
    private Long idProducto;
    private String nombreProducto;
    private Integer rating;
    private String comentario;
    private LocalDateTime fechaCreacion;
    private Boolean activo;
}
