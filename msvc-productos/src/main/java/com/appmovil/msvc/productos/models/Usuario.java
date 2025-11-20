package com.appmovil.msvc.productos.models;

import lombok.*;

import java.time.LocalDate;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private Long idUsuario;
    private String nombreCompleto;
    private String email;
    private String run; // Siguiendo el patrón de Edutech

    // Campos necesarios para autenticación/estado
    private String role;
    private Boolean cuentaActiva;

    // Constructor de ejemplo para uso en lógica interna
    public Usuario(Long idUsuario, String nombreCompleto) {
        this.idUsuario = idUsuario;
        this.nombreCompleto = nombreCompleto;
    }
}