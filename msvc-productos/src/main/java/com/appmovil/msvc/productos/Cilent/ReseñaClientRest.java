package com.appmovil.msvc.productos.Cilent;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient(name = "msvc-cursos", url = "localhost:8002/api/v1/curso")
public interface ReseñaClientRest {
    @GetMapping("/{id}")
    com.appmovil.msvc.productos.models.Reseña findById(@PathVariable Long id);
}
