package com.appmovil.msvc.admin.dtos;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDTO {
    
    private Long idProducto;
    
    @NotBlank(message = "El nombre del producto no puede estar vacío")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
    
    @NotNull(message = "El precio no puede ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que 0")
    private Double precio;
    
    @NotNull(message = "El stock no puede ser nulo")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;
    
    @NotBlank(message = "La categoría no puede estar vacía")
    private String categoria;
    
    private String imagen;
    
    private Boolean activo;
    
    @DecimalMin(value = "0.0", message = "El rating no puede ser negativo")
    @DecimalMax(value = "5.0", message = "El rating no puede ser mayor a 5")
    private Double rating;
}
