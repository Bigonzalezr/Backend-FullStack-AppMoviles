package com.appmovil.msvc.resenas.models;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    private Long id;
    private String nombre;
    private String email;
    private String password;
    private String telefono;
    private String direccion;
    private String rol;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
}
