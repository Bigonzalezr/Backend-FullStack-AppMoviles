package com.appmovil.msvc.usuarios.controllers;

import com.appmovil.msvc.usuarios.dtos.UsuarioCreationDTO;
import com.appmovil.msvc.usuarios.dtos.UsuarioDTO;
import com.appmovil.msvc.usuarios.dtos.UsuarioUpdateDTO;
import com.appmovil.msvc.usuarios.services.UsuarioService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@Validated
@Slf4j
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> findAll() {
        log.info("Obteniendo todos los usuarios");
        List<UsuarioDTO> usuarios = usuarioService.findAll();
        return ResponseEntity.ok(usuarios);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> findById(@PathVariable Long id) {
        log.info("Buscando usuario con id: {}", id);
        UsuarioDTO usuario = usuarioService.findById(id);
        return ResponseEntity.ok(usuario);
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<UsuarioDTO> findByUsername(@PathVariable String username) {
        log.info("Buscando usuario con username: {}", username);
        UsuarioDTO usuario = usuarioService.findByUsername(username);
        return ResponseEntity.ok(usuario);
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<UsuarioDTO> findByEmail(@PathVariable String email) {
        log.info("Buscando usuario con email: {}", email);
        UsuarioDTO usuario = usuarioService.findByEmail(email);
        return ResponseEntity.ok(usuario);
    }
    
    @GetMapping("/activos")
    public ResponseEntity<List<UsuarioDTO>> findByActivoTrue() {
        log.info("Obteniendo usuarios activos");
        List<UsuarioDTO> usuarios = usuarioService.findByActivoTrue();
        return ResponseEntity.ok(usuarios);
    }
    
    @GetMapping("/rol/{rol}")
    public ResponseEntity<List<UsuarioDTO>> findByRol(@PathVariable String rol) {
        log.info("Obteniendo usuarios con rol: {}", rol);
        List<UsuarioDTO> usuarios = usuarioService.findByRol(rol);
        return ResponseEntity.ok(usuarios);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<UsuarioDTO>> searchByName(@RequestParam String nombre) {
        log.info("Buscando usuarios con nombre: {}", nombre);
        List<UsuarioDTO> usuarios = usuarioService.searchByName(nombre);
        return ResponseEntity.ok(usuarios);
    }
    
    @PostMapping
    public ResponseEntity<UsuarioDTO> save(@Valid @RequestBody UsuarioCreationDTO usuarioCreationDTO) {
        log.info("Creando nuevo usuario: {}", usuarioCreationDTO.getUsername());
        UsuarioDTO usuario = usuarioService.save(usuarioCreationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> update(
            @PathVariable Long id, 
            @Valid @RequestBody UsuarioUpdateDTO usuarioUpdateDTO) {
        log.info("Actualizando usuario con id: {}", id);
        UsuarioDTO usuario = usuarioService.update(id, usuarioUpdateDTO);
        return ResponseEntity.ok(usuario);
    }
    
    @PatchMapping("/{id}/activar")
    public ResponseEntity<UsuarioDTO> activarUsuario(@PathVariable Long id) {
        log.info("Activando usuario con id: {}", id);
        UsuarioDTO usuario = usuarioService.activarUsuario(id);
        return ResponseEntity.ok(usuario);
    }
    
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<UsuarioDTO> desactivarUsuario(@PathVariable Long id) {
        log.info("Desactivando usuario con id: {}", id);
        UsuarioDTO usuario = usuarioService.desactivarUsuario(id);
        return ResponseEntity.ok(usuario);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        log.info("Eliminando usuario con id: {}", id);
        usuarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
