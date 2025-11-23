package com.appmovil.msvc.evaluaciones.clients;

import com.appmovil.msvc.evaluaciones.models.Alumno;
import com.appmovil.msvc.evaluaciones.models.Prueba;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "msvc-alumnos", url = "localhost:8001/api/v1/alumno")
public interface AlumnoClientRest {
    @GetMapping
    List<Alumno> findAll();

    @GetMapping("/{id}")
    Alumno findById(@PathVariable Long id);
}
