package com.appmovil.msvc.productos.dtos;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
public class ProductoDTO {
    private Long id;
    private String nombre;
    private Integer precio;
    private String categoria;
    private String imagen;
    private String descripcion;
    private Integer stock;
    private Double rating;
}
