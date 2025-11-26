package com.appmovil.msvc.productos.repositories;

import com.appmovil.msvc.productos.models.entities.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCategoriaIgnoreCase(String categoria);
    
    List<Producto> findByActivo(Boolean activo);
    
    List<Producto> findByCategoriaIgnoreCaseAndActivo(String categoria, Boolean activo);
    
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
}
