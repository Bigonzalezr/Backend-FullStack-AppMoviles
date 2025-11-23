package com.appmovil.msvc.cursos.repositories;

import com.appmovil.msvc.cursos.models.entities.Cursos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CursosRepository extends JpaRepository<Cursos,Long> {
}
