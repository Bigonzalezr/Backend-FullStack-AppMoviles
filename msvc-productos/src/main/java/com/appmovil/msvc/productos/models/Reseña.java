package com.appmovil.msvc.productos.models;

import lombok.*;

import java.time.LocalDate;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Reseña {

    private Long idReseña;
    private Long idProducto;
    private Long idUsuario;

    private Double rating;
    private String comentario;
    private LocalDate fecha;


    private String nombreUsuario;
}