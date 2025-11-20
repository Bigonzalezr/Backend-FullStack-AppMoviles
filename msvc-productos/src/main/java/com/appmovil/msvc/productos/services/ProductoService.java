package com.appmovil.msvc.productos.services;

import com.appmovil.msvc.productos.models.entities.Producto;
import com.appmovil.msvc.productos.dtos.ProductoUpdateDTO;
import java.util.List;

public interface ProductoService {



    Producto save(Producto producto);


    List<Producto> findAll();


    Producto findById(Long id);


    void delete(Long id);


    Producto update(Long id, ProductoUpdateDTO productoUpdateDTO);


    List<Producto> findByCategoria(String categoria);
}