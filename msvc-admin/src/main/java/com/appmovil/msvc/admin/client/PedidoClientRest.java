package com.appmovil.msvc.admin.client;

import com.appmovil.msvc.admin.models.Pedido;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "msvc-pedidos", url = "localhost:8006/api/v1/pedidos")
public interface PedidoClientRest {
    
    @GetMapping
    List<Pedido> findAll();
    
    @GetMapping("/{id}")
    Pedido findById(@PathVariable Long id);
    
    @PostMapping
    Pedido save(@RequestBody Pedido pedido);
    
    @PutMapping("/{id}")
    Pedido update(@PathVariable Long id, @RequestBody Pedido pedido);
    
    @DeleteMapping("/{id}")
    void deleteById(@PathVariable Long id);
    
    @GetMapping("/usuario/{idUsuario}")
    List<Pedido> findByUsuarioId(@PathVariable Long idUsuario);
    
    @GetMapping("/estado/{estado}")
    List<Pedido> findByEstado(@PathVariable String estado);
    
    @PatchMapping("/{id}/estado")
    Pedido updateEstado(@PathVariable Long id, @RequestParam String estado);
}
