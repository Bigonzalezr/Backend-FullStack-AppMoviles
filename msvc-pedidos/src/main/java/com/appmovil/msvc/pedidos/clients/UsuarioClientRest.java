package com.appmovil.msvc.pedidos.clients;

import com.appmovil.msvc.pedidos.models.Usuario;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "msvc-usuarios", url = "http://localhost:8001/api/usuarios")
public interface UsuarioClientRest {
    @GetMapping("/{id}")
    Usuario findById(@PathVariable("id") Long id);

    // Aquí se podrían añadir otros métodos como:
    // Usuario findByEmail(@RequestParam String email);
}