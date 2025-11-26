package com.appmovil.msvc.resenas.services;

import com.appmovil.msvc.resenas.dtos.ResenaDTO;
import com.appmovil.msvc.resenas.models.entities.Resena;

import java.util.List;

public interface ResenaService {
    List<ResenaDTO> findAll();
    
    ResenaDTO findById(Long id);
    
    ResenaDTO save(Resena Resena);
    
    ResenaDTO update(Long id, Resena Resena);
    
    void delete(Long id);
    
    List<ResenaDTO> findByUsuario(Long idUsuario);
    
    List<ResenaDTO> findByProducto(Long idProducto);
    
    Double getAverageRatingByProducto(Long idProducto);
}