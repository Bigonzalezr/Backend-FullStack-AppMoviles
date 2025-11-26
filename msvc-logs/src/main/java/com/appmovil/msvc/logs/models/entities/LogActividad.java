package com.appmovil.msvc.logs.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_actividad", indexes = {
    @Index(name = "idx_usuario", columnList = "id_usuario"),
    @Index(name = "idx_fecha", columnList = "fecha"),
    @Index(name = "idx_tipo", columnList = "tipo_actividad")
})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogActividad {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "tipo_actividad", nullable = false, length = 50)
    @NotBlank(message = "El tipo de actividad no puede estar vacío")
    private String tipoActividad; // LOGIN, LOGOUT, REGISTRO, COMPRA, VER_PRODUCTO, AGREGAR_CARRITO, etc.

    @Column(nullable = false)
    @NotNull(message = "La fecha no puede estar vacía")
    private LocalDateTime fecha;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "id_recurso")
    private Long idRecurso; // ID del producto, pedido, etc. relacionado

    @Column(name = "tipo_recurso", length = 50)
    private String tipoRecurso; // PRODUCTO, PEDIDO, CARRITO, etc.

    @Column(length = 20)
    private String resultado; // EXITOSO, FALLIDO, ERROR

    @Column(name = "datos_adicionales", columnDefinition = "TEXT")
    private String datosAdicionales; // JSON con información extra

    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
        if (resultado == null) {
            resultado = "EXITOSO";
        }
    }
}
