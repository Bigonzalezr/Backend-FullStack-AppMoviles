package com.appmovil.msvc.pedidos.dtos;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoDTO {
    private Long idPedido;
    private Long idUsuario;
    private String estado;
    private LocalDateTime fechaPedido;
    private String direccionEnvio;
    private String metodoPago;
    private String notas;
    private Integer subtotal;
    private Integer costoEnvio;
    private Integer total;
    
    // Campos enriquecidos
    private String nombreUsuario;
    private String emailUsuario;
    private List<PedidoDetalleDTO> detalles;
}