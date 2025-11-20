package com.appmovil.msvc.pedidos.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;


@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
public class Usuario {

    private Long id;
    private String nombre;
    private String apellidos;
    private String email;
    private String role;


    private String run;
    private Boolean cuentaActiva;
    private LocalDate fechaNacimiento;
}