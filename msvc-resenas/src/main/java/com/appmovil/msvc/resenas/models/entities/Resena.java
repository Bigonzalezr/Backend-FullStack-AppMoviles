package com.appmovil.msvc.resenas.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Resenas")
@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
public class Resena {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "id_usuario")
    @NotNull(message = "El usuario no puede ser vacío")
    private Long idUsuario;
    
    @Column(name = "id_producto")
    @NotNull(message = "El producto no puede ser vacío")
    private Long idProducto;
    
    @NotNull(message = "El rating no puede ser vacío")
    @Min(value = 1, message = "El rating mínimo es 1")
    @Max(value = 5, message = "El rating máximo es 5")
    private Integer rating;
    
    @NotBlank(message = "El comentario no puede estar vacío")
    @Size(min = 10, max = 500, message = "El comentario debe tener entre 10 y 500 caracteres")
    @Column(length = 500)
    private String comentario;
    
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "activo")
    private Boolean activo = true;
    
    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (activo == null) {
            activo = true;
        }
    }
}
