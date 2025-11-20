package com.appmovil.msvc.compra.services;

import com.edutech.msvc.compra.dtos.CompraDTO;

import java.util.List;

public interface CompraService {

    List<CompraDTO> findAll();

    com.edutech.msvc.compra.model.entity.Pedido findById(Long id);

    com.edutech.msvc.compra.model.entity.Pedido save(com.edutech.msvc.compra.model.entity.Pedido pedido);

    List<com.edutech.msvc.compra.model.entity.Pedido> findByAlumnoId(Long alumnoId);

    List<com.edutech.msvc.compra.model.entity.Pedido> findByProfesorId(Long profesorId);
}
