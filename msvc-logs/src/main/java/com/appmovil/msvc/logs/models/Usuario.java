package com.appmovil.msvc.logs.models;

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
}
