package com.appmovil.msvc.productos.Cilent;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "msvc-productos", url = "localhost:8083/api/v1/compra")
public interface UsuarioClientRest {

    @GetMapping("/{id}")
    com.appmovil.msvc.productos.models.Usuario findById(@PathVariable Long id);
}
