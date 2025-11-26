package com.appmovil.msvc.logs.services;

import com.appmovil.msvc.logs.dtos.LogActividadDTO;
import com.appmovil.msvc.logs.dtos.RegistrarLogDTO;
import com.appmovil.msvc.logs.models.entities.LogActividad;

import java.time.LocalDateTime;
import java.util.List;

public interface LogActividadService {
    
    LogActividad registrarLog(RegistrarLogDTO registrarLogDTO);
    
    List<LogActividadDTO> findAll();
    
    LogActividad findById(Long id);
    
    List<LogActividadDTO> findByUsuario(Long idUsuario);
    
    List<LogActividadDTO> findByTipoActividad(String tipoActividad);
    
    List<LogActividadDTO> findByFechaRango(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    List<LogActividadDTO> findByUsuarioYFecha(Long idUsuario, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    List<LogActividadDTO> findByRecurso(Long idRecurso, String tipoRecurso);
}
