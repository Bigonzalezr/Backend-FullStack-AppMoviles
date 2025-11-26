package com.appmovil.msvc.carrito.clients;

import com.appmovil.msvc.carrito.models.Usuario;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "msvc-usuarios", url = "localhost:8001/api/v1/usuarios")
public interface UsuarioClientRest {

    @GetMapping("/{id}")
    Usuario findById(@PathVariable Long id);
    
    @GetMapping("/username/{username}")
    Usuario findByUsername(@PathVariable String username);
}
