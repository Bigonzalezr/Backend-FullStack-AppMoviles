package com.appmovil.msvc.resenas.controllers;

import com.appmovil.msvc.resenas.dtos.ReseñaDTO;
import com.appmovil.msvc.resenas.models.entities.Reseña;
import com.appmovil.msvc.resenas.services.ReseñaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reseñas")
@Validated
public class ReseñaController {

    @Autowired
    private ReseñaService reseñaService;

    @GetMapping
    public ResponseEntity<List<ReseñaDTO>> findAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.reseñaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReseñaDTO> findById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.reseñaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ReseñaDTO> save(@RequestBody @Validated Reseña reseña) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.reseñaService.save(reseña));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReseñaDTO> update(@PathVariable Long id, @RequestBody @Validated Reseña reseña) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.reseñaService.update(id, reseña));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        this.reseñaService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/usuario/{id}")
    public ResponseEntity<List<ReseñaDTO>> findByUsuario(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.reseñaService.findByUsuario(id));
    }

    @GetMapping("/producto/{id}")
    public ResponseEntity<List<ReseñaDTO>> findByProducto(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.reseñaService.findByProducto(id));
    }

    @GetMapping("/producto/{id}/rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.reseñaService.getAverageRatingByProducto(id));
    }
}
