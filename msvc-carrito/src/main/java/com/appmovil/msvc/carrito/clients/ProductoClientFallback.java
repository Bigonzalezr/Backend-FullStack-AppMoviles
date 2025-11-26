package com.appmovil.msvc.carrito.clients;

import com.appmovil.msvc.carrito.models.Producto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ProductoClientFallback implements ProductoClientRest {

    @Override
    public Producto findById(Long id) {
        log.warn("Fallback activado para ProductoClient.findById({}). El servicio de productos no está disponible.", id);
        // Retornar producto por defecto para evitar fallo total
        Producto fallback = new Producto();
        fallback.setIdProducto(id);
        fallback.setNombre("Producto temporalmente no disponible");
        fallback.setPrecio(BigDecimal.ZERO);
        fallback.setStock(0);
        fallback.setActivo(false);
        return fallback;
    }

    @Override
    public List<Producto> findActivos() {
        log.warn("Fallback activado para ProductoClient.findActivos(). El servicio de productos no está disponible.");
        return new ArrayList<>();
    }

    @Override
    public void actualizarStock(Long id, Integer cantidad) {
        log.warn("Fallback activado para ProductoClient.actualizarStock({}, {}). El servicio de productos no está disponible.", id, cantidad);
        // No se puede actualizar stock, pero no lanzamos excepción para no romper el flujo
    }
}
