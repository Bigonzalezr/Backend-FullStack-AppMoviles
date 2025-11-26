package com.appmovil.msvc.productos.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "productos")
@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Column(nullable = false)
    @NotNull(message = "El precio es obligatorio")
    private Integer precio;

    @Column(nullable = false)
    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;

    @Lob //
    private String imagen; // 

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "El stock es obligatorio")
    private Integer stock;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Builder.Default
    private Double rating = 0.0;

    @PrePersist
    protected void onCreate() {
        if (activo == null) {
            activo = true;
        }
        if (rating == null) {
            rating = 0.0;
        }
    }
}