package com.appmovil.msvc.pagos.clients;

import com.appmovil.msvc.pagos.models.Pedido;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "msvc-pedidos", url = "http://localhost:8006/api/v1/pedidos")
public interface PedidoClientRest {

    @GetMapping("/{id}")
    Pedido findById(@PathVariable Long id);

    @PatchMapping("/{id}/estado-pago")
    void actualizarEstadoPago(@PathVariable Long id);
}
