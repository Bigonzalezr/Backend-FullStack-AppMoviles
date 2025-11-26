package com.appmovil.msvc.pedidos.model;

import lombok.*;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    private Long idUsuario;
    private String username;
    private String email;
    private String nombre;
    private String apellido;
    private String telefono;
    private Boolean activo;
}