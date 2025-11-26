package com.appmovil.msvc.productos.services;

import com.appmovil.msvc.productos.models.entities.Producto;
import com.appmovil.msvc.productos.dtos.ProductoUpdateDTO;
import java.util.List;

public interface ProductoService {

    Producto save(Producto producto);

    List<Producto> findAll();
    
    List<Producto> findActivos();

    Producto findById(Long id);

    void delete(Long id);

    Producto update(Long id, ProductoUpdateDTO productoUpdateDTO);
    
    Producto actualizarStock(Long id, Integer cantidad);

    List<Producto> findByCategoria(String categoria);
    
    List<Producto> buscarPorNombre(String nombre);
}