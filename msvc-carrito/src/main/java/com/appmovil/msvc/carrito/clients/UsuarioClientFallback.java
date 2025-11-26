package com.appmovil.msvc.carrito.clients;

import com.appmovil.msvc.carrito.models.Usuario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UsuarioClientFallback implements UsuarioClientRest {

    @Override
    public Usuario findById(Long id) {
        log.warn("Fallback activado para UsuarioClient.findById({}). El servicio de usuarios no está disponible.", id);
        Usuario fallback = new Usuario();
        fallback.setIdUsuario(id);
        fallback.setNombre("Usuario");
        fallback.setApellido("No Disponible");
        fallback.setActivo(false);
        return fallback;
    }

    @Override
    public Usuario findByUsername(String username) {
        log.warn("Fallback activado para UsuarioClient.findByUsername({}). El servicio de usuarios no está disponible.", username);
        Usuario fallback = new Usuario();
        fallback.setUsername(username);
        fallback.setNombre("Usuario");
        fallback.setApellido("No Disponible");
        fallback.setActivo(false);
        return fallback;
    }
}
