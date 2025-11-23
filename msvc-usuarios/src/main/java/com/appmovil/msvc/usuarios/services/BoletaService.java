package com.appmovil.msvc.boleta.services;

import com.appmovil.msvc.usuarios.dtos.BoletaDTO;
import com.appmovil.msvc.usuarios.models.entities.Boleta;

import java.util.List;

public interface BoletaService {

    List<BoletaDTO> findAll();
    Boleta findById(Long id);
    Boleta save(Boleta boleta);
    List<Boleta> findByAlumnoId(Long alumnoId);
    List<Boleta> findByProfesorId(Long profesorId);
    List<Boleta> findByCursoId(Long cursoId);

}
