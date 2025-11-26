package com.appmovil.msvc.resenas.clients;

import com.appmovil.msvc.resenas.models.Usuario;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "msvc-usuarios", url = "http://localhost:8008/api/v1/usuarios")
public interface UsuarioClientRest {
    @GetMapping
    List<Usuario> findAll();

    @GetMapping("/{id}")
    Usuario findById(@PathVariable Long id);
}
