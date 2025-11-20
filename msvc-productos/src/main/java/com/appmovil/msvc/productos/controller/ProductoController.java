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
// Ruta base para el frontend de React: /api/productos
@RequestMapping("/api/productos")
@Validated
// Nota: La documentación de Swagger (clases Operation, ApiResponses) se omitió para brevedad,
// pero debería ser añadida aquí, siguiendo el estilo de tu proyecto Edutech.
public class ProductoController {

    // Se inyecta la interfaz del Servicio (ProductoService)
    @Autowired
    private ProductoService productoService;

    // GET /api/productos
    @GetMapping
    public ResponseEntity<List<Producto>> findAll() {
        // Muestra todos los productos del catálogo
        List<Producto> productos = this.productoService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(productos);
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

    // --- NUEVO ENDPOINT DE NEGOCIO ---
    // GET /api/productos/categoria/{categoria}
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Producto>> findByCategoria(@PathVariable String categoria) {
        List<Producto> productos = this.productoService.findByCategoria(categoria);
        return ResponseEntity.status(HttpStatus.OK).body(productos);
    }
}