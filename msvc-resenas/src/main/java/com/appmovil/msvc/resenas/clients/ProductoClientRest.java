package com.appmovil.msvc.resenas.clients;

import com.appmovil.msvc.resenas.models.Producto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "msvc-productos", url = "http://localhost:8002/api/v1/productos")
public interface ProductoClientRest {
    @GetMapping
    List<Producto> findAll();

    @GetMapping("/{id}")
    Producto findById(@PathVariable Long id);
}
