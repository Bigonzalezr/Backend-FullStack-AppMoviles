package com.appmovil.msvc.logs.controller;

import com.appmovil.msvc.logs.dtos.LogActividadDTO;
import com.appmovil.msvc.logs.dtos.RegistrarLogDTO;
import com.appmovil.msvc.logs.models.entities.LogActividad;
import com.appmovil.msvc.logs.services.LogActividadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/logs")
@Validated
public class LogActividadController {

    @Autowired
    private LogActividadService logActividadService;

    @PostMapping
    public ResponseEntity<LogActividad> registrarLog(@Valid @RequestBody RegistrarLogDTO registrarLogDTO) {
        LogActividad log = logActividadService.registrarLog(registrarLogDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(log);
    }

    @GetMapping
    public ResponseEntity<List<LogActividadDTO>> findAll() {
        return ResponseEntity.ok(logActividadService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LogActividad> findById(@PathVariable Long id) {
        return ResponseEntity.ok(logActividadService.findById(id));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<LogActividadDTO>> findByUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(logActividadService.findByUsuario(idUsuario));
    }

    @GetMapping("/tipo/{tipoActividad}")
    public ResponseEntity<List<LogActividadDTO>> findByTipoActividad(@PathVariable String tipoActividad) {
        return ResponseEntity.ok(logActividadService.findByTipoActividad(tipoActividad));
    }

    @GetMapping("/fecha-rango")
    public ResponseEntity<List<LogActividadDTO>> findByFechaRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return ResponseEntity.ok(logActividadService.findByFechaRango(fechaInicio, fechaFin));
    }

    @GetMapping("/usuario/{idUsuario}/fecha-rango")
    public ResponseEntity<List<LogActividadDTO>> findByUsuarioYFecha(
            @PathVariable Long idUsuario,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return ResponseEntity.ok(logActividadService.findByUsuarioYFecha(idUsuario, fechaInicio, fechaFin));
    }

    @GetMapping("/recurso")
    public ResponseEntity<List<LogActividadDTO>> findByRecurso(
            @RequestParam Long idRecurso,
            @RequestParam String tipoRecurso) {
        return ResponseEntity.ok(logActividadService.findByRecurso(idRecurso, tipoRecurso));
    }
}
