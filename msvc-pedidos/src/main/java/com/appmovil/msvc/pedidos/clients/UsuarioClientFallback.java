package com.appmovil.msvc.pedidos.clients;

import com.appmovil.msvc.pedidos.model.Usuario;
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
        fallback.setEmail("nodisp@ejemplo.com");
        fallback.setActivo(false);
        return fallback;
    }

    @Override
    public void eliminarPedidoPorUsuario(Long idUsuario) {
        log.warn("Fallback activado para UsuarioClient.eliminarPedidoPorUsuario({}). El servicio de usuarios no está disponible.", idUsuario);
        // No se puede eliminar, pero no lanzamos excepción
    }
}
