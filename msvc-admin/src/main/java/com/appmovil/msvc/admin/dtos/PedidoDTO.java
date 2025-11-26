package com.appmovil.msvc.admin.dtos;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoDTO {
    
    private Long idPedido;
    
    @NotNull(message = "El ID del usuario no puede ser nulo")
    private Long idUsuario;
    
    private LocalDateTime fechaPedido;
    
    @NotBlank(message = "El estado no puede estar vacío")
    @Pattern(regexp = "PENDIENTE|CONFIRMADO|ENVIADO|ENTREGADO|CANCELADO", 
             message = "El estado debe ser: PENDIENTE, CONFIRMADO, ENVIADO, ENTREGADO o CANCELADO")
    private String estado;
    
    @NotNull(message = "El total no puede ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "El total debe ser mayor que 0")
    private Double total;
    
    @NotBlank(message = "La dirección de envío no puede estar vacía")
    private String direccionEnvio;
    
    @NotBlank(message = "El método de pago no puede estar vacío")
    private String metodoPago;
    
    // Información adicional del usuario (para visualización)
    private String nombreUsuario;
    private String emailUsuario;
}
