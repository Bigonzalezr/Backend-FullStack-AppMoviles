package com.appmovil.msvc.pedidos.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "pedidos_detalle") // Tabla para los ítems de la orden
@Getter @Setter @ToString(exclude = "pedido") // Excluir 'pedido' de ToString para evitar recursión
@NoArgsConstructor @AllArgsConstructor
public class PedidoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long id;

    @NotNull(message = "Debe referenciar un producto")
    private Long idProducto;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombreProducto;

    @NotNull(message = "El precio unitario es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Integer precioUnitario;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima es 1")
    private Integer cantidad;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    @JsonIgnore // Ignora este campo al serializar a JSON para evitar el ciclo infinito
    private Pedido pedido;

    @Transient
    public Integer getTotalLinea() {
        return precioUnitario * cantidad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PedidoDetalle that)) return false;

        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}