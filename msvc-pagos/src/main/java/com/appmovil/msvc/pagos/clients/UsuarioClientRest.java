package com.appmovil.msvc.pagos.clients;

import com.appmovil.msvc.pagos.models.Usuario;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "msvc-usuarios", url = "http://localhost:8008/api/v1/usuarios")
public interface UsuarioClientRest {

    @GetMapping("/{id}")
    Usuario findById(@PathVariable Long id);
}
