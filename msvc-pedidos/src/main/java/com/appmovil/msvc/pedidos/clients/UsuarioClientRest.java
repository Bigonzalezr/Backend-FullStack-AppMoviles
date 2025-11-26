package com.appmovil.msvc.pedidos.clients;

import com.appmovil.msvc.pedidos.model.Usuario;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;

@FeignClient(name = "msvc-usuarios", url = "http://localhost:8001/api/v1/usuarios")
public interface UsuarioClientRest {

    @GetMapping("/{id}")
    Usuario findById(@PathVariable("id") Long id);


    @DeleteMapping("/usuario/{idUsuario}")
    void eliminarPedidoPorUsuario(@PathVariable("idUsuario") Long idUsuario);
}