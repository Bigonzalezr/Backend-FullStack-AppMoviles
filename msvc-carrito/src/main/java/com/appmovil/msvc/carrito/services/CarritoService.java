package com.appmovil.msvc.carrito.services;

import com.appmovil.msvc.carrito.dtos.CarritoDTO;
import com.appmovil.msvc.carrito.dtos.AgregarItemDTO;
import com.appmovil.msvc.carrito.models.entities.Carrito;

public interface CarritoService {
    
    Carrito obtenerCarritoActivo(Long idUsuario);
    
    CarritoDTO obtenerCarritoConDetalles(Long idUsuario);
    
    Carrito agregarItem(Long idUsuario, AgregarItemDTO agregarItemDTO);
    
    Carrito actualizarCantidadItem(Long idUsuario, Long idProducto, Integer cantidad);
    
    Carrito removerItem(Long idUsuario, Long idProducto);
    
    void vaciarCarrito(Long idUsuario);
    
    Carrito crearCarrito(Long idUsuario);
}
