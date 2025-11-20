package com.appmovil.msvc.productos.Cilent;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient(name = "msvc-evaluaciones", url = "localhost:8003/api/v1/evaluaciones")
public interface PedidoClientRest {
    @GetMapping("/{id}")
    com.appmovil.msvc.productos.models.Pedido findById(@PathVariable Long id);
}
