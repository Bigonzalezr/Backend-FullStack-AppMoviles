package com.appmovil.msvc.usuarios.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    
    private String token;
    
    @Builder.Default
    private String type = "Bearer";
    private Long usuarioId;
    private String username;
    private String email;
    private String rol;
    
    public AuthResponse(String token, Long usuarioId, String username, String email, String rol) {
        this.token = token;
        this.type = "Bearer";
        this.usuarioId = usuarioId;
        this.username = username;
        this.email = email;
        this.rol = rol;
    }
}
