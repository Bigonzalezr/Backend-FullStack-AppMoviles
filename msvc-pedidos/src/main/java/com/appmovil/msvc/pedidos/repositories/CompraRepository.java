package com.edutech.msvc.compra.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<com.edutech.msvc.compra.model.entity.Pedido, Long> {

    List<com.edutech.msvc.compra.model.entity.Pedido> findByIdAlumno(Long idAlumno);

    List<com.edutech.msvc.compra.model.entity.Pedido> findByIdProfesor(Long idProfesor);
}
