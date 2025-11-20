package com.appmovil.msvc.productos.Cilent;

import com.appmovil.msvc.productos.models.Inscripcion;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "msvc-inscripcion", url = "localhost:8004/api/v1/inscripcion")
public interface CarritoClientRest {
    @GetMapping("/{id}")
    List<Inscripcion> findByIdAlumno(@PathVariable Long id);
}
