package com.appmovil.msvc.usuarios.services;

import com.appmovil.msvc.usuarios.dtos.UsuarioCreationDTO;
import com.appmovil.msvc.usuarios.dtos.UsuarioDTO;
import com.appmovil.msvc.usuarios.dtos.UsuarioUpdateDTO;
import com.appmovil.msvc.usuarios.exceptions.DuplicateResourceException;
import com.appmovil.msvc.usuarios.exceptions.ResourceNotFoundException;
import com.appmovil.msvc.usuarios.models.entities.Usuario;
import com.appmovil.msvc.usuarios.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - Unit Tests")
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioTest;
    private UsuarioCreationDTO creationDTO;
    private UsuarioUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        usuarioTest = Usuario.builder()
                .idUsuario(1L)
                .username("testuser")
                .email("test@email.com")
                .password("encodedPassword123")
                .nombre("Test")
                .apellido("User")
                .telefono("123456789")
                .direccion("Test Address 123")
                .rol("USER")
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .build();

        creationDTO = new UsuarioCreationDTO();
        creationDTO.setUsername("newuser");
        creationDTO.setEmail("newuser@email.com");
        creationDTO.setPassword("password123");
        creationDTO.setNombre("New");
        creationDTO.setApellido("User");
        creationDTO.setTelefono("987654321");
        creationDTO.setDireccion("New Address 456");
        creationDTO.setRol("USER");

        updateDTO = new UsuarioUpdateDTO();
        updateDTO.setNombre("Updated");
        updateDTO.setApellido("Name");
        updateDTO.setTelefono("111222333");
    }

    @Test
    @DisplayName("findAll - Debe retornar lista de todos los usuarios")
    void testFindAll_DebeRetornarListaUsuarios() {
        // Given
        Usuario usuario2 = Usuario.builder()
                .idUsuario(2L)
                .username("user2")
                .email("user2@email.com")
                .password("pass")
                .nombre("User")
                .apellido("Two")
                .rol("USER")
                .activo(true)
                .build();
        
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuarioTest, usuario2));

        // When
        List<UsuarioDTO> resultado = usuarioService.findAll();

        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getUsername()).isEqualTo("testuser");
        assertThat(resultado.get(1).getUsername()).isEqualTo("user2");
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findById - Debe retornar usuario cuando existe")
    void testFindById_CuandoUsuarioExiste_DebeRetornarUsuario() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioTest));

        // When
        UsuarioDTO resultado = usuarioService.findById(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getUsername()).isEqualTo("testuser");
        assertThat(resultado.getEmail()).isEqualTo("test@email.com");
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando usuario no existe")
    void testFindById_CuandoUsuarioNoExiste_DebeLanzarExcepcion() {
        // Given
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> usuarioService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario")
                .hasMessageContaining("999");
        
        verify(usuarioRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("findByUsername - Debe retornar usuario cuando existe")
    void testFindByUsername_CuandoExiste_DebeRetornarUsuario() {
        // Given
        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuarioTest));

        // When
        UsuarioDTO resultado = usuarioService.findByUsername("testuser");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getUsername()).isEqualTo("testuser");
        verify(usuarioRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("findByUsername - Debe lanzar excepción cuando no existe")
    void testFindByUsername_CuandoNoExiste_DebeLanzarExcepcion() {
        // Given
        when(usuarioRepository.findByUsername("noexiste")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> usuarioService.findByUsername("noexiste"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario")
                .hasMessageContaining("noexiste");
    }

    @Test
    @DisplayName("create - Debe crear usuario exitosamente con password encriptado")
    void testCreate_DebeCrearUsuarioConPasswordEncriptado() {
        // Given
        when(usuarioRepository.existsByUsername(creationDTO.getUsername())).thenReturn(false);
        when(usuarioRepository.existsByEmail(creationDTO.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(creationDTO.getPassword())).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setIdUsuario(1L);
            usuario.setFechaRegistro(LocalDateTime.now());
            return usuario;
        });

        // When
        UsuarioDTO resultado = usuarioService.create(creationDTO);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getUsername()).isEqualTo("newuser");
        assertThat(resultado.getEmail()).isEqualTo("newuser@email.com");
        assertThat(resultado.getActivo()).isTrue();
        
        verify(usuarioRepository).existsByUsername("newuser");
        verify(usuarioRepository).existsByEmail("newuser@email.com");
        verify(passwordEncoder).encode("password123");
        verify(usuarioRepository).save(argThat(usuario -> 
            usuario.getPassword().equals("encodedPassword") &&
            usuario.getActivo() &&
            usuario.getRol().equals("USER")
        ));
    }

    @Test
    @DisplayName("create - Debe lanzar excepción cuando username ya existe")
    void testCreate_CuandoUsernameExiste_DebeLanzarExcepcion() {
        // Given
        when(usuarioRepository.existsByUsername(creationDTO.getUsername())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> usuarioService.create(creationDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Usuario")
                .hasMessageContaining("username")
                .hasMessageContaining("newuser");
        
        verify(usuarioRepository).existsByUsername("newuser");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - Debe lanzar excepción cuando email ya existe")
    void testCreate_CuandoEmailExiste_DebeLanzarExcepcion() {
        // Given
        when(usuarioRepository.existsByUsername(creationDTO.getUsername())).thenReturn(false);
        when(usuarioRepository.existsByEmail(creationDTO.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> usuarioService.create(creationDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Usuario")
                .hasMessageContaining("email")
                .hasMessageContaining("newuser@email.com");
        
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe actualizar usuario exitosamente")
    void testUpdate_DebeActualizarUsuario() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioTest));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UsuarioDTO resultado = usuarioService.update(1L, updateDTO);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Updated");
        assertThat(resultado.getApellido()).isEqualTo("Name");
        assertThat(resultado.getTelefono()).isEqualTo("111222333");
        
        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository).save(argThat(usuario ->
            usuario.getNombre().equals("Updated") &&
            usuario.getApellido().equals("Name") &&
            usuario.getTelefono().equals("111222333")
        ));
    }

    @Test
    @DisplayName("activarUsuario - Debe activar usuario inactivo")
    void testActivarUsuario_DebeActivarUsuario() {
        // Given
        usuarioTest.setActivo(false);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioTest));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UsuarioDTO resultado = usuarioService.activarUsuario(1L);

        // Then
        assertThat(resultado.getActivo()).isTrue();
        verify(usuarioRepository).save(argThat(usuario -> usuario.getActivo()));
    }

    @Test
    @DisplayName("desactivarUsuario - Debe desactivar usuario activo")
    void testDesactivarUsuario_DebeDesactivarUsuario() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioTest));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UsuarioDTO resultado = usuarioService.desactivarUsuario(1L);

        // Then
        assertThat(resultado.getActivo()).isFalse();
        verify(usuarioRepository).save(argThat(usuario -> !usuario.getActivo()));
    }

    @Test
    @DisplayName("deleteById - Debe verificar existencia antes de eliminar")
    void testDeleteById_DebeVerificarExistenciaAntesDeEliminar() {
        // Given
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        // When
        usuarioService.deleteById(1L);

        // Then
        verify(usuarioRepository).existsById(1L);
        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteById - Debe lanzar excepción cuando usuario no existe")
    void testDeleteById_CuandoNoExiste_DebeLanzarExcepcion() {
        // Given
        when(usuarioRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> usuarioService.deleteById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario")
                .hasMessageContaining("999");
        
        verify(usuarioRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("findByActivoTrue - Debe retornar solo usuarios activos")
    void testFindByActivoTrue_DebeRetornarSoloUsuariosActivos() {
        // Given
        Usuario usuarioInactivo = Usuario.builder()
                .idUsuario(2L)
                .username("inactivo")
                .email("inactivo@email.com")
                .password("pass")
                .nombre("User")
                .apellido("Inactivo")
                .rol("USER")
                .activo(false)
                .build();
        
        when(usuarioRepository.findByActivoTrue()).thenReturn(Arrays.asList(usuarioTest));

        // When
        List<UsuarioDTO> resultado = usuarioService.findByActivoTrue();

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getActivo()).isTrue();
        assertThat(resultado.get(0).getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("findByRol - Debe retornar usuarios del rol especificado")
    void testFindByRol_DebeRetornarUsuariosDelRol() {
        // Given
        Usuario admin = Usuario.builder()
                .idUsuario(2L)
                .username("admin")
                .email("admin@email.com")
                .password("pass")
                .nombre("Admin")
                .apellido("User")
                .rol("ADMIN")
                .activo(true)
                .build();
        
        when(usuarioRepository.findByRol("ADMIN")).thenReturn(Arrays.asList(admin));

        // When
        List<UsuarioDTO> resultado = usuarioService.findByRol("ADMIN");

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getRol()).isEqualTo("ADMIN");
        assertThat(resultado.get(0).getUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("searchByName - Debe buscar por nombre o apellido")
    void testSearchByName_DebeBuscarPorNombreOApellido() {
        // Given
        when(usuarioRepository.searchByNameOrLastName("Test")).thenReturn(Arrays.asList(usuarioTest));

        // When
        List<UsuarioDTO> resultado = usuarioService.searchByName("Test");

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).contains("Test");
        verify(usuarioRepository).searchByNameOrLastName("Test");
    }

    @Test
    @DisplayName("create - Debe asignar rol USER por defecto cuando no se especifica")
    void testCreate_DebeAsignarRolUserPorDefecto() {
        // Given
        creationDTO.setRol(null);
        when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setIdUsuario(1L);
            return usuario;
        });

        // When
        UsuarioDTO resultado = usuarioService.create(creationDTO);

        // Then
        assertThat(resultado.getRol()).isEqualTo("USER");
        verify(usuarioRepository).save(argThat(usuario -> usuario.getRol().equals("USER")));
    }
}
