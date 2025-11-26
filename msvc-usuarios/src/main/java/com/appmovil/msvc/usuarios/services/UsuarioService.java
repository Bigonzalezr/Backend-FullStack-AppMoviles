package com.appmovil.msvc.usuarios.services;

import com.appmovil.msvc.usuarios.dtos.UsuarioCreationDTO;
import com.appmovil.msvc.usuarios.dtos.UsuarioDTO;
import com.appmovil.msvc.usuarios.dtos.UsuarioUpdateDTO;

import java.util.List;

public interface UsuarioService {
    
    List<UsuarioDTO> findAll();
    
    UsuarioDTO findById(Long id);
    
    UsuarioDTO findByUsername(String username);
    
    UsuarioDTO findByEmail(String email);
    
    UsuarioDTO save(UsuarioCreationDTO usuarioCreationDTO);
    
    UsuarioDTO create(UsuarioCreationDTO usuarioCreationDTO);
    
    UsuarioDTO update(Long id, UsuarioUpdateDTO usuarioUpdateDTO);
    
    void deleteById(Long id);
    
    List<UsuarioDTO> findByActivoTrue();
    
    List<UsuarioDTO> findByRol(String rol);
    
    List<UsuarioDTO> searchByName(String searchTerm);
    
    UsuarioDTO activarUsuario(Long id);
    
    UsuarioDTO desactivarUsuario(Long id);
}
