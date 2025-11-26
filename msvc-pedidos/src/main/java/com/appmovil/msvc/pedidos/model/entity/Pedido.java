package com.appmovil.msvc.pedidos.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long id;

    @NotNull(message = "El ID de usuario es obligatorio")
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(nullable = false)
    private String estado = "PENDIENTE";

    @Column(name = "fecha_pedido", nullable = false)
    @NotNull(message = "La fecha del pedido es obligatoria")
    private LocalDateTime fechaPedido = LocalDateTime.now();

    @Column(name = "direccion_envio", length = 500)
    private String direccionEnvio;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    // Campos de resumen financiero
    private Integer subtotal;
    private Integer costoEnvio;
    private Integer total;


    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoDetalle> detalles = new ArrayList<>();



    public void agregarDetalle(PedidoDetalle detalle) {
        detalles.add(detalle);
        detalle.setPedido(this);
    }


    @PrePersist
    @PreUpdate
    public void calcularTotales() {
        this.subtotal = detalles.stream()
                .mapToInt(d -> d.getPrecioUnitario() * d.getCantidad())
                .sum();

        this.costoEnvio = (this.subtotal > 50000) ? 0 : 5000;

        this.total = this.subtotal + this.costoEnvio;
    }

    public void agregarItem(Long idProducto, String nombreProducto, Integer precioUnitario, Integer cantidad) {
        PedidoDetalle detalle = new PedidoDetalle();
        detalle.setIdProducto(idProducto);
        detalle.setNombreProducto(nombreProducto);
        detalle.setPrecioUnitario(precioUnitario);
        detalle.setCantidad(cantidad);
        this.agregarDetalle(detalle);
    }
}