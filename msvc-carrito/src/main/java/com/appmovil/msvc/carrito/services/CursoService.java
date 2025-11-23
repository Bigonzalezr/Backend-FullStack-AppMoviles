package com.appmovil.msvc.cursos.services;

import com.appmovil.msvc.cursos.dtos.InscripcionCursoDTO;
import com.appmovil.msvc.cursos.models.entities.Curso;

import java.util.List;

public interface CursoService {
    List<Curso> findAll();
    Curso findById(Long id);
    Curso save(Curso curso);
    List<InscripcionCursoDTO> findInscripcionesById(Long cursoId);
}
