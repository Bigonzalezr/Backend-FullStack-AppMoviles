package com.appmovil.msvc.pedidos.clients;

import com.appmovil.msvc.pedidos.model.Pago;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "msvc-pagos", url = "http://localhost:8005/api/v1/pagos")
public interface PagoClientRest {

    @PostMapping
    Pago registrarPago(@RequestBody Pago pago);

    @GetMapping("/{id}")
    Pago findById(@PathVariable("id") Long id);
}