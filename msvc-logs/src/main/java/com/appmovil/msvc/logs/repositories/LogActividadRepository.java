package com.appmovil.msvc.logs.repositories;

import com.appmovil.msvc.logs.models.entities.LogActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogActividadRepository extends JpaRepository<LogActividad, Long> {
    
    List<LogActividad> findByIdUsuario(Long idUsuario);
    
    List<LogActividad> findByTipoActividad(String tipoActividad);
    
    List<LogActividad> findByIdUsuarioAndTipoActividad(Long idUsuario, String tipoActividad);
    
    List<LogActividad> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    List<LogActividad> findByIdUsuarioAndFechaBetween(Long idUsuario, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    List<LogActividad> findByIdRecursoAndTipoRecurso(Long idRecurso, String tipoRecurso);
    
    List<LogActividad> findByResultado(String resultado);
    
    @Query("SELECT l FROM LogActividad l WHERE l.idUsuario = :idUsuario ORDER BY l.fecha DESC")
    List<LogActividad> findRecentActivityByUsuario(@Param("idUsuario") Long idUsuario);
}
