package com.appmovil.msvc.admin.controller;

import com.appmovil.msvc.admin.models.admin;
import com.appmovil.msvc.admin.models.Producto;
import com.appmovil.msvc.admin.models.Pedido;
import com.appmovil.msvc.admin.services.AdminService;
import com.appmovil.msvc.admin.client.ProductoClientRest;
import com.appmovil.msvc.admin.client.PedidoClientRest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@Validated
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private ProductoClientRest productoClientRest;
    
    @Autowired
    private PedidoClientRest pedidoClientRest;

    // ========== Endpoints de Admin ==========
    @GetMapping
    public ResponseEntity<List<Admin>> findAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(adminService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> findById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(adminService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Admin> save(@Valid @RequestBody Admin admin) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(adminService.save(admin));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Admin> update(@PathVariable Long id, @Valid @RequestBody Admin admin) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(adminService.updateById(id, admin));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        adminService.deleteById(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    // ========== Gestión de Productos ==========
    @GetMapping("/productos")
    public ResponseEntity<List<Producto>> getAllProductos() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(productoClientRest.findAll());
    }

    @GetMapping("/productos/{id}")
    public ResponseEntity<Producto> getProductoById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(productoClientRest.findById(id));
    }

    @PostMapping("/productos")
    public ResponseEntity<Producto> createProducto(@Valid @RequestBody Producto producto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productoClientRest.save(producto));
    }

    @PutMapping("/productos/{id}")
    public ResponseEntity<Producto> updateProducto(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(productoClientRest.update(id, producto));
    }

    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> deleteProducto(@PathVariable Long id) {
        productoClientRest.deleteById(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/productos/categoria/{categoria}")
    public ResponseEntity<List<Producto>> getProductosByCategoria(@PathVariable String categoria) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(productoClientRest.findByCategoria(categoria));
    }

    // ========== Gestión de Pedidos ==========
    @GetMapping("/pedidos")
    public ResponseEntity<List<Pedido>> getAllPedidos() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(pedidoClientRest.findAll());
    }

    @GetMapping("/pedidos/{id}")
    public ResponseEntity<Pedido> getPedidoById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(pedidoClientRest.findById(id));
    }

    @GetMapping("/pedidos/usuario/{idUsuario}")
    public ResponseEntity<List<Pedido>> getPedidosByUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(pedidoClientRest.findByUsuarioId(idUsuario));
    }

    @GetMapping("/pedidos/estado/{estado}")
    public ResponseEntity<List<Pedido>> getPedidosByEstado(@PathVariable String estado) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(pedidoClientRest.findByEstado(estado));
    }

    @PatchMapping("/pedidos/{id}/estado")
    public ResponseEntity<Pedido> updateEstadoPedido(@PathVariable Long id, @RequestParam String estado) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(pedidoClientRest.updateEstado(id, estado));
    }

    @DeleteMapping("/pedidos/{id}")
    public ResponseEntity<Void> deletePedido(@PathVariable Long id) {
        pedidoClientRest.deleteById(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
