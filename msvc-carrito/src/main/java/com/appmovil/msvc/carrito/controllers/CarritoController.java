package com.appmovil.msvc.carrito.controllers;

import com.appmovil.msvc.carrito.dtos.AgregarItemDTO;
import com.appmovil.msvc.carrito.dtos.ActualizarCantidadDTO;
import com.appmovil.msvc.carrito.dtos.CarritoDTO;
import com.appmovil.msvc.carrito.models.entities.Carrito;
import com.appmovil.msvc.carrito.services.CarritoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carrito")
@Validated
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<CarritoDTO> obtenerCarrito(@PathVariable Long idUsuario) {
        CarritoDTO carrito = carritoService.obtenerCarritoConDetalles(idUsuario);
        return ResponseEntity.ok(carrito);
    }

    @PostMapping("/usuario/{idUsuario}/item")
    public ResponseEntity<Carrito> agregarItem(
            @PathVariable Long idUsuario,
            @Valid @RequestBody AgregarItemDTO agregarItemDTO) {
        Carrito carrito = carritoService.agregarItem(idUsuario, agregarItemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(carrito);
    }

    @PutMapping("/usuario/{idUsuario}/item/{idProducto}")
    public ResponseEntity<Carrito> actualizarCantidad(
            @PathVariable Long idUsuario,
            @PathVariable Long idProducto,
            @Valid @RequestBody ActualizarCantidadDTO actualizarCantidadDTO) {
        Carrito carrito = carritoService.actualizarCantidadItem(
                idUsuario, 
                idProducto, 
                actualizarCantidadDTO.getCantidad()
        );
        return ResponseEntity.ok(carrito);
    }

    @DeleteMapping("/usuario/{idUsuario}/item/{idProducto}")
    public ResponseEntity<Carrito> removerItem(
            @PathVariable Long idUsuario,
            @PathVariable Long idProducto) {
        Carrito carrito = carritoService.removerItem(idUsuario, idProducto);
        return ResponseEntity.ok(carrito);
    }

    @DeleteMapping("/usuario/{idUsuario}")
    public ResponseEntity<Void> vaciarCarrito(@PathVariable Long idUsuario) {
        carritoService.vaciarCarrito(idUsuario);
        return ResponseEntity.noContent().build();
    }
}
