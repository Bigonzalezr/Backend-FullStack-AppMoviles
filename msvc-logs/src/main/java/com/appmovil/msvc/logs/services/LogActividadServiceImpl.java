package com.appmovil.msvc.logs.services;

import com.appmovil.msvc.logs.clients.UsuarioClientRest;
import com.appmovil.msvc.logs.dtos.LogActividadDTO;
import com.appmovil.msvc.logs.dtos.RegistrarLogDTO;
import com.appmovil.msvc.logs.exceptions.LogException;
import com.appmovil.msvc.logs.exceptions.ResourceNotFoundException;
import com.appmovil.msvc.logs.models.Usuario;
import com.appmovil.msvc.logs.models.entities.LogActividad;
import com.appmovil.msvc.logs.repositories.LogActividadRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogActividadServiceImpl implements LogActividadService {

    @Autowired
    private LogActividadRepository logActividadRepository;

    @Autowired
    private UsuarioClientRest usuarioClientRest;

    @Override
    public LogActividad registrarLog(RegistrarLogDTO registrarLogDTO) {
        LogActividad log = LogActividad.builder()
                .idUsuario(registrarLogDTO.getIdUsuario())
                .tipoActividad(registrarLogDTO.getTipoActividad())
                .descripcion(registrarLogDTO.getDescripcion())
                .ipAddress(registrarLogDTO.getIpAddress())
                .userAgent(registrarLogDTO.getUserAgent())
                .idRecurso(registrarLogDTO.getIdRecurso())
                .tipoRecurso(registrarLogDTO.getTipoRecurso())
                .resultado(registrarLogDTO.getResultado())
                .datosAdicionales(registrarLogDTO.getDatosAdicionales())
                .build();
        
        return logActividadRepository.save(log);
    }

    @Override
    public List<LogActividadDTO> findAll() {
        return logActividadRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public LogActividad findById(Long id) {
        return logActividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Log", "id", id.toString()));
    }

    @Override
    public List<LogActividadDTO> findByUsuario(Long idUsuario) {
        return logActividadRepository.findByIdUsuario(idUsuario).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LogActividadDTO> findByTipoActividad(String tipoActividad) {
        return logActividadRepository.findByTipoActividad(tipoActividad).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LogActividadDTO> findByFechaRango(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return logActividadRepository.findByFechaBetween(fechaInicio, fechaFin).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LogActividadDTO> findByUsuarioYFecha(Long idUsuario, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return logActividadRepository.findByIdUsuarioAndFechaBetween(idUsuario, fechaInicio, fechaFin).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LogActividadDTO> findByRecurso(Long idRecurso, String tipoRecurso) {
        return logActividadRepository.findByIdRecursoAndTipoRecurso(idRecurso, tipoRecurso).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private LogActividadDTO convertirADTO(LogActividad log) {
        Usuario usuario = null;
        if (log.getIdUsuario() != null) {
            try {
                usuario = usuarioClientRest.findById(log.getIdUsuario());
            } catch (FeignException ex) {
                // Usuario no encontrado, continuar sin datos de usuario
            }
        }

        return LogActividadDTO.builder()
                .idLog(log.getIdLog())
                .idUsuario(log.getIdUsuario())
                .usernameUsuario(usuario != null ? usuario.getUsername() : "Desconocido")
                .tipoActividad(log.getTipoActividad())
                .fecha(log.getFecha())
                .descripcion(log.getDescripcion())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .idRecurso(log.getIdRecurso())
                .tipoRecurso(log.getTipoRecurso())
                .resultado(log.getResultado())
                .datosAdicionales(log.getDatosAdicionales())
                .build();
    }
}
