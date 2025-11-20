package com.appmovil.msvc.pedidos.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos") // La tabla se llamará 'pedidos'
@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long id;

    @NotNull(message = "El ID de usuario es obligatorio")
    @Column(name = "id_usuario")
    private Long idUsuario; // Reemplaza a idAlumno

    @Column(nullable = false)
    private String estado = "PENDIENTE"; // Estado de la orden (PENDIENTE, ENVIADO, CANCELADO)

    @Column(name = "fecha_compra", nullable = false)
    @NotNull(message = "La fecha de compra es obligatoria")
    private LocalDateTime fechaCompra = LocalDateTime.now();

    // Campos de resumen financiero
    private Integer subtotal;
    private Integer costoEnvio;
    private Integer totalFinal;

    // Relación de uno a muchos: Un Pedido tiene muchos PedidoDetalle (los productos comprados)
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoDetalle> detalles = new ArrayList<>();


    @PrePersist @PreUpdate
    public void calcularTotales() {
        this.subtotal = detalles.stream().mapToInt(PedidoDetalle::getTotalLinea).sum();

        // Lógica de envío: gratis si subtotal > $200.000
        this.costoEnvio = (this.subtotal > 200000) ? 0 : 3000;

        this.totalFinal = this.subtotal + this.costoEnvio;
    }

    public void agregarDetalle(PedidoDetalle detalle) {
        detalles.add(detalle);
        detalle.setPedido(this);
    }
}