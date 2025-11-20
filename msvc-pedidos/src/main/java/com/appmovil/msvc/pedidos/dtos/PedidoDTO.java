package com.appmovil.msvc.pedidos.dtos;

import com.appmovil.msvc.pedidos.models.Usuario;
import com.appmovil.msvc.pedidos.models.Producto;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

// DTO para representar el pedido en una respuesta GET (listado o detalle)
@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
public class PedidoDTO {

    private Long idPedido;
    private String estado;
    private LocalDateTime fechaCompra;
    private Integer subtotal;
    private Integer costoEnvio;
    private Integer totalFinal;

    // Campos enriquecidos (vienen de otros MSVCs)
    private Usuario usuario;
    private List<Producto> productos; // Lista de productos comprados
}