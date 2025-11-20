package com.appmovil.msvc.productos.models;

import com.appmovil.msvc.productos.models.CarritoItem;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

// Clase que representa una orden de compra generada.
// No es una entidad en este microservicio.
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    private Long idPedido;
    private Long idUsuario;
    private String estado;

    private Integer subtotal;
    private Integer costoEnvio;
    private Integer totalFinal;

    private LocalDateTime fechaCompra;


    private List<CarritoItem> itemsComprados;
}