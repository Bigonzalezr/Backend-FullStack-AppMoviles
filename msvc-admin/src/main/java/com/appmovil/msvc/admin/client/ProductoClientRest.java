package com.appmovil.msvc.admin.client;

import com.appmovil.msvc.admin.models.Producto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "msvc-productos", url = "localhost:8002/api/v1/productos")
public interface ProductoClientRest {
    
    @GetMapping
    List<Producto> findAll();
    
    @GetMapping("/{id}")
    Producto findById(@PathVariable Long id);
    
    @PostMapping
    Producto save(@RequestBody Producto producto);
    
    @PutMapping("/{id}")
    Producto update(@PathVariable Long id, @RequestBody Producto producto);
    
    @DeleteMapping("/{id}")
    void deleteById(@PathVariable Long id);
    
    @GetMapping("/categoria/{categoria}")
    List<Producto> findByCategoria(@PathVariable String categoria);
    
    @GetMapping("/activos")
    List<Producto> findByActivoTrue();
}
