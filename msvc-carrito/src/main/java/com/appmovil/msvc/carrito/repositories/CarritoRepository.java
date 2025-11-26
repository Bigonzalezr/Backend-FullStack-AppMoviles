package com.appmovil.msvc.carrito.repositories;

import com.appmovil.msvc.carrito.models.entities.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    
    Optional<Carrito> findByIdUsuarioAndEstado(Long idUsuario, String estado);
    
    Optional<Carrito> findByIdUsuario(Long idUsuario);
}
