package com.appmovil.msvc.carrito.repositories;

import com.appmovil.msvc.carrito.models.entities.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {
    
    List<ItemCarrito> findByCarritoIdCarrito(Long idCarrito);
    
    Optional<ItemCarrito> findByCarritoIdCarritoAndIdProducto(Long idCarrito, Long idProducto);
    
    void deleteByCarritoIdCarrito(Long idCarrito);
}
