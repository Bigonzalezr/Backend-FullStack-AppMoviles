package com.appmovil.msvc.pedidos.clients;

import com.appmovil.msvc.pedidos.model.Producto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductoClientFallback implements ProductoClientRest {

    @Override
    public Producto findById(Long id) {
        log.warn("Fallback activado para ProductoClient.findById({}). El servicio de productos no está disponible.", id);
        Producto fallback = new Producto();
        fallback.setIdProducto(id);
        fallback.setNombre("Producto no disponible");
        fallback.setPrecio(0);
        fallback.setStock(0);
        fallback.setActivo(false);
        return fallback;
    }

    @Override
    public Producto updateStock(Long id, Integer cantidad) {
        log.warn("Fallback activado para ProductoClient.updateStock({}, {}). El servicio de productos no está disponible.", id, cantidad);
        Producto fallback = new Producto();
        fallback.setIdProducto(id);
        fallback.setNombre("Producto no disponible");
        fallback.setPrecio(0);
        fallback.setStock(0);
        fallback.setActivo(false);
        return fallback;
    }

    @Override
    public void eliminarPedidoPorProducto(Long idProducto) {
        log.warn("Fallback activado para ProductoClient.eliminarPedidoPorProducto({}). El servicio de productos no está disponible.", idProducto);
        // No se puede eliminar
    }
}
