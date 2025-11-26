package com.appmovil.msvc.resenas.controllers;

import com.appmovil.msvc.resenas.dtos.ResenaDTO;
import com.appmovil.msvc.resenas.models.entities.Resena;
import com.appmovil.msvc.resenas.services.ResenaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/Resenas")
@Validated
public class ResenaController {

    @Autowired
    private ResenaService resenaService;

    @GetMapping
    public ResponseEntity<List<ResenaDTO>> findAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.resenaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResenaDTO> findById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.resenaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ResenaDTO> save(@RequestBody @Validated Resena Resena) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.resenaService.save(Resena));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResenaDTO> update(@PathVariable Long id, @RequestBody @Validated Resena Resena) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.resenaService.update(id, Resena));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        this.resenaService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/usuario/{id}")
    public ResponseEntity<List<ResenaDTO>> findByUsuario(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.resenaService.findByUsuario(id));
    }

    @GetMapping("/producto/{id}")
    public ResponseEntity<List<ResenaDTO>> findByProducto(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.resenaService.findByProducto(id));
    }

    @GetMapping("/producto/{id}/rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.resenaService.getAverageRatingByProducto(id));
    }
}
