package com.appmovil.msvc.resenas.services;

import com.appmovil.msvc.resenas.dtos.ReseñaDTO;
import com.appmovil.msvc.resenas.models.entities.Reseña;

import java.util.List;

public interface ReseñaService {
    List<ReseñaDTO> findAll();
    
    ReseñaDTO findById(Long id);
    
    ReseñaDTO save(Reseña reseña);
    
    ReseñaDTO update(Long id, Reseña reseña);
    
    void delete(Long id);
    
    List<ReseñaDTO> findByUsuario(Long idUsuario);
    
    List<ReseñaDTO> findByProducto(Long idProducto);
    
    Double getAverageRatingByProducto(Long idProducto);
}