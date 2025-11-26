package com.appmovil.msvc.admin.services;

import com.appmovil.msvc.admin.exception.AdminException;
import com.appmovil.msvc.admin.models.entities.Admin;
import com.appmovil.msvc.admin.repositories.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private AdminServicelmpl adminService;

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = Admin.builder()
                .idAdmin(1L)
                .username("admin1")
                .email("admin1@test.com")
                .password("password123")
                .nombre("Admin")
                .apellido("Test")
                .telefono("123456789")
                .activo(true)
                .rol("ADMIN")
                .build();
    }

    @Test
    @DisplayName("Debe obtener todos los administradores")
    void testFindAll() {
        // Given
        Admin admin2 = Admin.builder()
                .idAdmin(2L)
                .username("admin2")
                .email("admin2@test.com")
                .password("password456")
                .nombre("Admin2")
                .apellido("Test2")
                .activo(true)
                .rol("MODERADOR")
                .build();

        when(adminRepository.findAll()).thenReturn(Arrays.asList(admin, admin2));

        // When
        List<Admin> result = adminService.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("admin1");
        assertThat(result.get(1).getUsername()).isEqualTo("admin2");
        assertThat(result.get(0).getRol()).isEqualTo("ADMIN");
        assertThat(result.get(1).getRol()).isEqualTo("MODERADOR");

        verify(adminRepository).findAll();
    }

    @Test
    @DisplayName("Debe encontrar administrador por ID")
    void testFindById() {
        // Given
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));

        // When
        Admin result = adminService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIdAdmin()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("admin1");
        assertThat(result.getEmail()).isEqualTo("admin1@test.com");

        verify(adminRepository).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando administrador no existe")
    void testFindByIdNotFound() {
        // Given
        when(adminRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adminService.findById(999L))
                .isInstanceOf(AdminException.class)
                .hasMessageContaining("999")
                .hasMessageContaining("no se encuentra");

        verify(adminRepository).findById(999L);
    }

    @Test
    @DisplayName("Debe guardar un nuevo administrador exitosamente")
    void testSave() {
        // Given
        Admin newAdmin = Admin.builder()
                .username("newadmin")
                .email("newadmin@test.com")
                .password("password123")
                .nombre("New")
                .apellido("Admin")
                .telefono("987654321")
                .activo(true)
                .rol("ADMIN")
                .build();

        when(adminRepository.existsByUsername("newadmin")).thenReturn(false);
        when(adminRepository.existsByEmail("newadmin@test.com")).thenReturn(false);
        when(adminRepository.save(any(Admin.class))).thenReturn(newAdmin);

        // When
        Admin result = adminService.save(newAdmin);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newadmin");
        assertThat(result.getEmail()).isEqualTo("newadmin@test.com");

        verify(adminRepository).existsByUsername("newadmin");
        verify(adminRepository).existsByEmail("newadmin@test.com");
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando username ya existe")
    void testSaveUsernameDuplicado() {
        // Given
        Admin newAdmin = Admin.builder()
                .username("admin1")
                .email("newemail@test.com")
                .password("password123")
                .nombre("Test")
                .apellido("Admin")
                .build();

        when(adminRepository.existsByUsername("admin1")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> adminService.save(newAdmin))
                .isInstanceOf(AdminException.class)
                .hasMessageContaining("nombre de usuario")
                .hasMessageContaining("ya está en uso");

        verify(adminRepository).existsByUsername("admin1");
        verify(adminRepository, never()).existsByEmail(anyString());
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando email ya existe")
    void testSaveEmailDuplicado() {
        // Given
        Admin newAdmin = Admin.builder()
                .username("newusername")
                .email("admin1@test.com")
                .password("password123")
                .nombre("Test")
                .apellido("Admin")
                .build();

        when(adminRepository.existsByUsername("newusername")).thenReturn(false);
        when(adminRepository.existsByEmail("admin1@test.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> adminService.save(newAdmin))
                .isInstanceOf(AdminException.class)
                .hasMessageContaining("email")
                .hasMessageContaining("ya está en uso");

        verify(adminRepository).existsByUsername("newusername");
        verify(adminRepository).existsByEmail("admin1@test.com");
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    @DisplayName("Debe actualizar administrador exitosamente")
    void testUpdateById() {
        // Given
        Admin adminActualizado = Admin.builder()
                .username("admin1_updated")
                .email("admin1_updated@test.com")
                .nombre("AdminUpdated")
                .apellido("TestUpdated")
                .telefono("999999999")
                .activo(false)
                .rol("SUPER_ADMIN")
                .build();

        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(adminRepository.save(any(Admin.class))).thenReturn(admin);

        // When
        Admin result = adminService.updateById(1L, adminActualizado);

        // Then
        assertThat(result).isNotNull();
        verify(adminRepository).findById(1L);
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    @DisplayName("Debe actualizar administrador sin cambiar password si no se proporciona")
    void testUpdateByIdSinPassword() {
        // Given
        Admin adminActualizado = Admin.builder()
                .username("admin1_updated")
                .email("admin1_updated@test.com")
                .nombre("AdminUpdated")
                .apellido("TestUpdated")
                .activo(true)
                .rol("ADMIN")
                .password(null) // Password null, no debe actualizarse
                .build();

        Admin adminExistente = Admin.builder()
                .idAdmin(1L)
                .username("admin1")
                .email("admin1@test.com")
                .password("oldpassword")
                .nombre("Admin")
                .apellido("Test")
                .activo(true)
                .rol("ADMIN")
                .build();

        when(adminRepository.findById(1L)).thenReturn(Optional.of(adminExistente));
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Admin result = adminService.updateById(1L, adminActualizado);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("admin1_updated");
        // Password no debe cambiar cuando es null
        verify(adminRepository).findById(1L);
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    @DisplayName("Debe actualizar password cuando se proporciona")
    void testUpdateByIdConPassword() {
        // Given
        Admin adminActualizado = Admin.builder()
                .username("admin1")
                .email("admin1@test.com")
                .nombre("Admin")
                .apellido("Test")
                .password("newpassword123")
                .activo(true)
                .rol("ADMIN")
                .build();

        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Admin result = adminService.updateById(1L, adminActualizado);

        // Then
        assertThat(result).isNotNull();
        verify(adminRepository).findById(1L);
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar administrador inexistente")
    void testUpdateByIdNotFound() {
        // Given
        Admin adminActualizado = Admin.builder()
                .username("admin1")
                .email("admin1@test.com")
                .nombre("Admin")
                .apellido("Test")
                .build();

        when(adminRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adminService.updateById(999L, adminActualizado))
                .isInstanceOf(AdminException.class)
                .hasMessageContaining("999")
                .hasMessageContaining("no encontrado");

        verify(adminRepository).findById(999L);
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    @DisplayName("Debe eliminar administrador exitosamente")
    void testDeleteById() {
        // Given
        when(adminRepository.existsById(1L)).thenReturn(true);
        doNothing().when(adminRepository).deleteById(1L);

        // When
        adminService.deleteById(1L);

        // Then
        verify(adminRepository).existsById(1L);
        verify(adminRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar administrador inexistente")
    void testDeleteByIdNotFound() {
        // Given
        when(adminRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> adminService.deleteById(999L))
                .isInstanceOf(AdminException.class)
                .hasMessageContaining("999")
                .hasMessageContaining("no encontrado");

        verify(adminRepository).existsById(999L);
        verify(adminRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay administradores")
    void testFindAllVacio() {
        // Given
        when(adminRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<Admin> result = adminService.findAll();

        // Then
        assertThat(result).isEmpty();

        verify(adminRepository).findAll();
    }

    @Test
    @DisplayName("Debe guardar administrador con rol SUPER_ADMIN")
    void testSaveSuperAdmin() {
        // Given
        Admin superAdmin = Admin.builder()
                .username("superadmin")
                .email("superadmin@test.com")
                .password("password123")
                .nombre("Super")
                .apellido("Admin")
                .activo(true)
                .rol("SUPER_ADMIN")
                .build();

        when(adminRepository.existsByUsername("superadmin")).thenReturn(false);
        when(adminRepository.existsByEmail("superadmin@test.com")).thenReturn(false);
        when(adminRepository.save(any(Admin.class))).thenReturn(superAdmin);

        // When
        Admin result = adminService.save(superAdmin);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRol()).isEqualTo("SUPER_ADMIN");

        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    @DisplayName("Debe guardar administrador con rol MODERADOR")
    void testSaveModerador() {
        // Given
        Admin moderador = Admin.builder()
                .username("moderador")
                .email("moderador@test.com")
                .password("password123")
                .nombre("Moderador")
                .apellido("Test")
                .activo(true)
                .rol("MODERADOR")
                .build();

        when(adminRepository.existsByUsername("moderador")).thenReturn(false);
        when(adminRepository.existsByEmail("moderador@test.com")).thenReturn(false);
        when(adminRepository.save(any(Admin.class))).thenReturn(moderador);

        // When
        Admin result = adminService.save(moderador);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRol()).isEqualTo("MODERADOR");

        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    @DisplayName("Debe actualizar estado activo del administrador")
    void testUpdateEstadoActivo() {
        // Given
        Admin adminDesactivado = Admin.builder()
                .username("admin1")
                .email("admin1@test.com")
                .nombre("Admin")
                .apellido("Test")
                .activo(false)
                .rol("ADMIN")
                .build();

        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Admin result = adminService.updateById(1L, adminDesactivado);

        // Then
        assertThat(result).isNotNull();
        verify(adminRepository).findById(1L);
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    @DisplayName("Debe actualizar teléfono del administrador")
    void testUpdateTelefono() {
        // Given
        Admin adminConNuevoTelefono = Admin.builder()
                .username("admin1")
                .email("admin1@test.com")
                .nombre("Admin")
                .apellido("Test")
                .telefono("111222333")
                .activo(true)
                .rol("ADMIN")
                .build();

        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Admin result = adminService.updateById(1L, adminConNuevoTelefono);

        // Then
        assertThat(result).isNotNull();
        verify(adminRepository).findById(1L);
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    @DisplayName("Debe guardar administrador sin teléfono")
    void testSaveSinTelefono() {
        // Given
        Admin adminSinTelefono = Admin.builder()
                .username("adminnotel")
                .email("adminnotel@test.com")
                .password("password123")
                .nombre("Admin")
                .apellido("Sin Telefono")
                .telefono(null)
                .activo(true)
                .rol("ADMIN")
                .build();

        when(adminRepository.existsByUsername("adminnotel")).thenReturn(false);
        when(adminRepository.existsByEmail("adminnotel@test.com")).thenReturn(false);
        when(adminRepository.save(any(Admin.class))).thenReturn(adminSinTelefono);

        // When
        Admin result = adminService.save(adminSinTelefono);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTelefono()).isNull();

        verify(adminRepository).save(any(Admin.class));
    }
}
