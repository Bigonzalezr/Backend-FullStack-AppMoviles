package com.appmovil.msvc.usuarios.controllers;

import com.appmovil.msvc.usuarios.dtos.AuthResponse;
import com.appmovil.msvc.usuarios.dtos.LoginRequest;
import com.appmovil.msvc.usuarios.dtos.UsuarioCreationDTO;
import com.appmovil.msvc.usuarios.dtos.UsuarioDTO;
import com.appmovil.msvc.usuarios.models.entities.Usuario;
import com.appmovil.msvc.usuarios.security.JwtTokenProvider;
import com.appmovil.msvc.usuarios.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);
        
        UsuarioDTO usuario = usuarioService.findByUsername(loginRequest.getUsername());
        
        AuthResponse authResponse = new AuthResponse(
                jwt,
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getRol()
        );
        
        return ResponseEntity.ok(authResponse);
    }
    
    @PostMapping("/register")
    public ResponseEntity<UsuarioDTO> registerUser(@Valid @RequestBody UsuarioCreationDTO usuarioCreationDTO) {
        UsuarioDTO nuevoUsuario = usuarioService.create(usuarioCreationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
    }
    
    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        UsuarioDTO usuario = usuarioService.findByUsername(username);
        return ResponseEntity.ok(usuario);
    }
}
