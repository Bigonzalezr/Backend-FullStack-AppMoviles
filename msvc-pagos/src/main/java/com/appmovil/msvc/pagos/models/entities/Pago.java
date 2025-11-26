package com.appmovil.msvc.pagos.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago", indexes = {
    @Index(name = "idx_pedido", columnList = "id_pedido"),
    @Index(name = "idx_usuario", columnList = "id_usuario"),
    @Index(name = "idx_estado", columnList = "estado")
})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;

    @Column(name = "id_pedido", nullable = false)
    @NotNull(message = "El ID del pedido no puede estar vacío")
    private Long idPedido;

    @Column(name = "id_usuario", nullable = false)
    @NotNull(message = "El ID del usuario no puede estar vacío")
    private Long idUsuario;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El monto no puede estar vacío")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @Column(name = "metodo_pago", nullable = false, length = 50)
    @NotBlank(message = "El método de pago no puede estar vacío")
    private String metodoPago; // TARJETA_CREDITO, TARJETA_DEBITO, PAYPAL, TRANSFERENCIA, etc.

    @Column(nullable = false, length = 20)
    @NotBlank(message = "El estado no puede estar vacío")
    private String estado = "PENDIENTE"; // PENDIENTE, PROCESANDO, COMPLETADO, FALLIDO, REEMBOLSADO

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Column(name = "fecha_procesamiento")
    private LocalDateTime fechaProcesamiento;

    @Column(name = "numero_transaccion", unique = true, length = 100)
    private String numeroTransaccion;

    @Column(name = "numero_autorizacion", length = 50)
    private String numeroAutorizacion;

    @Column(name = "gateway_pago", length = 50)
    private String gatewayPago; // Stripe, PayPal, MercadoPago, etc.

    @Column(name = "ultimos_digitos_tarjeta", length = 4)
    private String ultimosDigitosTarjeta;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String mensajeError;

    @PrePersist
    protected void onCreate() {
        if (fechaPago == null) {
            fechaPago = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "PENDIENTE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if ("COMPLETADO".equals(estado) && fechaProcesamiento == null) {
            fechaProcesamiento = LocalDateTime.now();
        }
    }
}
