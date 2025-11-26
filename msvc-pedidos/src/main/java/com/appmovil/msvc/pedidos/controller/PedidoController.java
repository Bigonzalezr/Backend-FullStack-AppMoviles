package com.appmovil.msvc.pedidos.controller;

import com.appmovil.msvc.pedidos.dtos.PedidoCreationDTO;
import com.appmovil.msvc.pedidos.dtos.PedidoDTO;
import com.appmovil.msvc.pedidos.services.PedidoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pedidos")
@Validated
@Slf4j
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoDTO> crearPedido(@RequestBody @Valid PedidoCreationDTO creationDTO) {
        log.info("Recibida solicitud para crear pedido: {}", creationDTO);
        PedidoDTO pedido = pedidoService.crearPedido(creationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> findById(@PathVariable Long id) {
        PedidoDTO pedido = pedidoService.findById(id);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping
    public ResponseEntity<List<PedidoDTO>> findAll() {
        List<PedidoDTO> pedidos = pedidoService.findAll();
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<PedidoDTO>> findByUsuario(@PathVariable Long idUsuario) {
        List<PedidoDTO> pedidos = pedidoService.findByUsuario(idUsuario);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PedidoDTO>> findByEstado(@PathVariable String estado) {
        List<PedidoDTO> pedidos = pedidoService.findByEstado(estado);
        return ResponseEntity.ok(pedidos);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoDTO> actualizarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String nuevoEstado = body.get("estado");
        PedidoDTO pedido = pedidoService.actualizarEstado(id, nuevoEstado);
        return ResponseEntity.ok(pedido);
    }

    @PatchMapping("/{id}/estado-pago")
    public ResponseEntity<Void> actualizarEstadoPago(@PathVariable Long id) {
        log.info("Actualizando estado de pago del pedido: {}", id);
        pedidoService.actualizarEstadoPago(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarPedido(@PathVariable Long id) {
        log.info("Cancelando pedido: {}", id);
        pedidoService.cancelarPedido(id);
        return ResponseEntity.noContent().build();
    }
}