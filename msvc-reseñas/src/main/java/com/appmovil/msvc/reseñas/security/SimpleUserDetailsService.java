package com.appmovil.msvc.reseñas.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Servicio simplificado para validar tokens JWT.
 * No necesita acceso a base de datos porque el token ya contiene la información del usuario.
 */
@Service
public class SimpleUserDetailsService implements UserDetailsService {
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Retorna un UserDetails básico solo con el username
        // La autenticación real ya fue validada por el JWT
        return User.builder()
                .username(username)
                .password("") // Password vacío porque ya fue validado en msvc-usuarios
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
