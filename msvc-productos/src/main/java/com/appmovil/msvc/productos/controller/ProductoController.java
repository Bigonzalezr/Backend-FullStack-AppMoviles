package com.appmovil.msvc.productos.controller;

import com.appmovil.msvc.productos.models.entities.Producto;
import com.appmovil.msvc.productos.services.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@Validated
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<Producto>> findAll() {
        List<Producto> productos = this.productoService.findAll();
        return ResponseEntity.ok(productos);
    }
    
    @GetMapping("/activos")
    public ResponseEntity<List<Producto>> findActivos() {
        List<Producto> productos = this.productoService.findActivos();
        return ResponseEntity.ok(productos);
    }

    // GET /api/productos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Producto> findById(@PathVariable Long id) {
        // Muestra el detalle de un producto específico
        Producto producto = this.productoService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(producto);
    }

    // POST /api/productos (Crea un nuevo producto - Requiere rol de Admin)
    @PostMapping
    public ResponseEntity<Producto> save(@Valid @RequestBody Producto producto) {
        // Guarda la nueva entidad en la base de datos.
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productoService.save(producto));
    }

    // PUT /api/productos/{id} (Actualiza un producto existente - Requiere rol de Admin)
    @PutMapping("/{id}")
    // En el método del servicio, gestionaremos si es una actualización o una creación
    public ResponseEntity<Producto> update(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        // Aseguramos que el ID del path se use en el cuerpo para la actualización
        producto.setId(id);
        return ResponseEntity
                .status(HttpStatus.OK) // Devolvemos 200 OK para una actualización
                .body(productoService.save(producto));
    }

    // DELETE /api/productos/{id} (Elimina un producto - Requiere rol de Admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        productoService.delete(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT) // Código 204 indica eliminación exitosa
                .build();
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Producto>> findByCategoria(@PathVariable String categoria) {
        List<Producto> productos = this.productoService.findByCategoria(categoria);
        return ResponseEntity.ok(productos);
    }
    
    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscarPorNombre(@RequestParam String nombre) {
        List<Producto> productos = this.productoService.buscarPorNombre(nombre);
        return ResponseEntity.ok(productos);
    }
    
    @PutMapping("/{id}/stock")
    public ResponseEntity<Producto> actualizarStock(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {
        Producto producto = this.productoService.actualizarStock(id, cantidad);
        return ResponseEntity.ok(producto);
    }
}