package com.appmovil.msvc.usuarios.services;

import com.appmovil.msvc.usuarios.dtos.UsuarioCreationDTO;
import com.appmovil.msvc.usuarios.dtos.UsuarioDTO;
import com.appmovil.msvc.usuarios.dtos.UsuarioUpdateDTO;
import com.appmovil.msvc.usuarios.exceptions.DuplicateResourceException;
import com.appmovil.msvc.usuarios.exceptions.ResourceNotFoundException;
import com.appmovil.msvc.usuarios.models.entities.Usuario;
import com.appmovil.msvc.usuarios.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> findAll() {
        return usuarioRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO findById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return convertToDTO(usuario);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO findByUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));
        return convertToDTO(usuario);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO findByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));
        return convertToDTO(usuario);
    }
    
    @Override
    @Transactional
    public UsuarioDTO save(UsuarioCreationDTO usuarioCreationDTO) {
        // Verificar si el username ya existe
        if (usuarioRepository.existsByUsername(usuarioCreationDTO.getUsername())) {
            throw new DuplicateResourceException("Usuario", "username", usuarioCreationDTO.getUsername());
        }
        
        // Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(usuarioCreationDTO.getEmail())) {
            throw new DuplicateResourceException("Usuario", "email", usuarioCreationDTO.getEmail());
        }
        
        Usuario usuario = Usuario.builder()
                .username(usuarioCreationDTO.getUsername())
                .email(usuarioCreationDTO.getEmail())
                .password(usuarioCreationDTO.getPassword()) // TODO: Encriptar password
                .nombre(usuarioCreationDTO.getNombre())
                .apellido(usuarioCreationDTO.getApellido())
                .telefono(usuarioCreationDTO.getTelefono())
                .direccion(usuarioCreationDTO.getDireccion())
                .rol(usuarioCreationDTO.getRol() != null ? usuarioCreationDTO.getRol() : "USER")
                .activo(true)
                .build();
        
        Usuario savedUsuario = usuarioRepository.save(usuario);
        return convertToDTO(savedUsuario);
    }
    
    @Override
    @Transactional
    public UsuarioDTO update(Long id, UsuarioUpdateDTO usuarioUpdateDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        
        // Actualizar solo los campos proporcionados
        if (usuarioUpdateDTO.getEmail() != null) {
            // Verificar si el nuevo email ya existe en otro usuario
            if (!usuario.getEmail().equals(usuarioUpdateDTO.getEmail()) && 
                usuarioRepository.existsByEmail(usuarioUpdateDTO.getEmail())) {
                throw new DuplicateResourceException("Usuario", "email", usuarioUpdateDTO.getEmail());
            }
            usuario.setEmail(usuarioUpdateDTO.getEmail());
        }
        
        if (usuarioUpdateDTO.getPassword() != null) {
            usuario.setPassword(usuarioUpdateDTO.getPassword()); // TODO: Encriptar password
        }
        
        if (usuarioUpdateDTO.getNombre() != null) {
            usuario.setNombre(usuarioUpdateDTO.getNombre());
        }
        
        if (usuarioUpdateDTO.getApellido() != null) {
            usuario.setApellido(usuarioUpdateDTO.getApellido());
        }
        
        if (usuarioUpdateDTO.getTelefono() != null) {
            usuario.setTelefono(usuarioUpdateDTO.getTelefono());
        }
        
        if (usuarioUpdateDTO.getDireccion() != null) {
            usuario.setDireccion(usuarioUpdateDTO.getDireccion());
        }
        
        Usuario updatedUsuario = usuarioRepository.save(usuario);
        return convertToDTO(updatedUsuario);
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario", "id", id);
        }
        usuarioRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> findByActivoTrue() {
        return usuarioRepository.findByActivoTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> findByRol(String rol) {
        return usuarioRepository.findByRol(rol).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> searchByName(String searchTerm) {
        return usuarioRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(searchTerm, searchTerm)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public UsuarioDTO activarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        usuario.setActivo(true);
        Usuario updatedUsuario = usuarioRepository.save(usuario);
        return convertToDTO(updatedUsuario);
    }
    
    @Override
    @Transactional
    public UsuarioDTO desactivarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        usuario.setActivo(false);
        Usuario updatedUsuario = usuarioRepository.save(usuario);
        return convertToDTO(updatedUsuario);
    }
    
    private UsuarioDTO convertToDTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .telefono(usuario.getTelefono())
                .direccion(usuario.getDireccion())
                .activo(usuario.getActivo())
                .rol(usuario.getRol())
                .fechaRegistro(usuario.getFechaRegistro() != null ? usuario.getFechaRegistro().format(formatter) : null)
                .fechaActualizacion(usuario.getFechaActualizacion() != null ? usuario.getFechaActualizacion().format(formatter) : null)
                .build();
    }
}
