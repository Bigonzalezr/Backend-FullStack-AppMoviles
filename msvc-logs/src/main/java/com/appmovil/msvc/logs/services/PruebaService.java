package com.appmovil.msvc.prueba.services;

import com.appmovil.msvc.prueba.dtos.PruebaDTO;
import com.appmovil.msvc.prueba.models.entities.Prueba;

import java.util.List;

public interface PruebaService {

    List<PruebaDTO> findAll();
    Prueba findById(Long id);
    Prueba save(Prueba prueba);
    List<Prueba> findByIdCurso(Long cursoId);
    List<Prueba> findByIdProfesor(Long profesorId);
}
