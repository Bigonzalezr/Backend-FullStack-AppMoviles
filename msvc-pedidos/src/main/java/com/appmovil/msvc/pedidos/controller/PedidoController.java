package com.appmovil.msvc.pedidos.controller;

import com.appmovil.msvc.pedidos.dtos.PedidoDTO;
import com.appmovil.msvc.pedidos.model.entity.Pedido;
import com.appmovil.msvc.pedidos.services.PedidoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos") //
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;


    @GetMapping
    public ResponseEntity<List<PedidoDTO>> findAll() {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.pedidoService.findAll());
    }


    @GetMapping("/{id}")
    public ResponseEntity<Pedido> findById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.pedidoService.findById(id));
    }


    @PostMapping
    public ResponseEntity<Pedido> save(@RequestBody @Valid Pedido pedido) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.pedidoService.save(pedido));
    }


    @GetMapping("/usuario/{id}")
    public ResponseEntity<List<Pedido>> findByIdUsuario(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.pedidoService.findByIdUsuario(id));
    }


    @GetMapping("/producto/{id}")
    public ResponseEntity<List<Pedido>> findByProductoId(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.pedidoService.findByProductoId(id));
    }
}