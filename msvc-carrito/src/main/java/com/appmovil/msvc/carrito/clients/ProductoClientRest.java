package com.appmovil.msvc.carrito.clients;

import com.appmovil.msvc.carrito.models.Producto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "msvc-productos", url = "localhost:8002/api/v1/productos")
public interface ProductoClientRest {

    @GetMapping("/{id}")
    Producto findById(@PathVariable Long id);
    
    @GetMapping("/activos")
    List<Producto> findActivos();
    
    @PutMapping("/{id}/stock")
    void actualizarStock(@PathVariable Long id, @RequestParam Integer cantidad);
}
