package com.appmovil.msvc.pedidos.clients;

import com.appmovil.msvc.pedidos.model.Producto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "msvc-productos", url = "http://localhost:8002/api/v1/productos", fallback = ProductoClientFallback.class)
public interface ProductoClientRest {

    @GetMapping("/{id}")
    Producto findById(@PathVariable("id") Long id);


    @PutMapping("/{id}/stock")
    Producto updateStock(@PathVariable("id") Long id, @RequestParam("cantidad") Integer cantidad);

    @PutMapping("/producto/{idProducto}")
    void eliminarPedidoPorProducto(@PathVariable("idProducto") Long idProducto);
}