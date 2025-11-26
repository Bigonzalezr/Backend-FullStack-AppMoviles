package com.appmovil.msvc.logs.services;

import com.appmovil.msvc.logs.clients.UsuarioClientRest;
import com.appmovil.msvc.logs.dtos.LogActividadDTO;
import com.appmovil.msvc.logs.dtos.RegistrarLogDTO;
import com.appmovil.msvc.logs.exceptions.ResourceNotFoundException;
import com.appmovil.msvc.logs.models.Usuario;
import com.appmovil.msvc.logs.models.entities.LogActividad;
import com.appmovil.msvc.logs.repositories.LogActividadRepository;
import feign.FeignException;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogActividadServiceImplTest {

    @Mock
    private LogActividadRepository logActividadRepository;

    @Mock
    private UsuarioClientRest usuarioClientRest;

    @InjectMocks
    private LogActividadServiceImpl logActividadService;

    private LogActividad logActividad;
    private Usuario usuario;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setUsername("testuser");
        usuario.setEmail("testuser@test.com");

        logActividad = LogActividad.builder()
                .idLog(1L)
                .idUsuario(1L)
                .tipoActividad("LOGIN")
                .fecha(now)
                .descripcion("Usuario inició sesión")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .idRecurso(null)
                .tipoRecurso(null)
                .resultado("EXITOSO")
                .datosAdicionales("{\"device\":\"mobile\"}")
                .build();
    }

    @Test
    @DisplayName("Debe registrar un nuevo log exitosamente")
    void testRegistrarLog() {
        // Given
        RegistrarLogDTO dto = RegistrarLogDTO.builder()
                .idUsuario(1L)
                .tipoActividad("LOGIN")
                .descripcion("Usuario inició sesión")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .resultado("EXITOSO")
                .datosAdicionales("{\"device\":\"mobile\"}")
                .build();

        when(logActividadRepository.save(any(LogActividad.class))).thenReturn(logActividad);

        // When
        LogActividad result = logActividadService.registrarLog(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIdUsuario()).isEqualTo(1L);
        assertThat(result.getTipoActividad()).isEqualTo("LOGIN");
        assertThat(result.getDescripcion()).isEqualTo("Usuario inició sesión");
        assertThat(result.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(result.getResultado()).isEqualTo("EXITOSO");

        verify(logActividadRepository).save(any(LogActividad.class));
    }

    @Test
    @DisplayName("Debe registrar log con datos de recurso")
    void testRegistrarLogConRecurso() {
        // Given
        RegistrarLogDTO dto = RegistrarLogDTO.builder()
                .idUsuario(1L)
                .tipoActividad("VER_PRODUCTO")
                .descripcion("Usuario vio producto")
                .ipAddress("192.168.1.1")
                .idRecurso(100L)
                .tipoRecurso("PRODUCTO")
                .resultado("EXITOSO")
                .build();

        LogActividad logConRecurso = LogActividad.builder()
                .idLog(2L)
                .idUsuario(1L)
                .tipoActividad("VER_PRODUCTO")
                .fecha(now)
                .descripcion("Usuario vio producto")
                .ipAddress("192.168.1.1")
                .idRecurso(100L)
                .tipoRecurso("PRODUCTO")
                .resultado("EXITOSO")
                .build();

        when(logActividadRepository.save(any(LogActividad.class))).thenReturn(logConRecurso);

        // When
        LogActividad result = logActividadService.registrarLog(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIdRecurso()).isEqualTo(100L);
        assertThat(result.getTipoRecurso()).isEqualTo("PRODUCTO");
        assertThat(result.getTipoActividad()).isEqualTo("VER_PRODUCTO");

        verify(logActividadRepository).save(any(LogActividad.class));
    }

    @Test
    @DisplayName("Debe obtener todos los logs con enriquecimiento de usuario")
    void testFindAll() {
        // Given
        LogActividad log1 = LogActividad.builder()
                .idLog(1L)
                .idUsuario(1L)
                .tipoActividad("LOGIN")
                .fecha(now)
                .descripcion("Login exitoso")
                .resultado("EXITOSO")
                .build();

        LogActividad log2 = LogActividad.builder()
                .idLog(2L)
                .idUsuario(1L)
                .tipoActividad("LOGOUT")
                .fecha(now.plusHours(1))
                .descripcion("Logout")
                .resultado("EXITOSO")
                .build();

        when(logActividadRepository.findAll()).thenReturn(Arrays.asList(log1, log2));
        when(usuarioClientRest.findById(1L)).thenReturn(usuario);

        // When
        List<LogActividadDTO> result = logActividadService.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIdLog()).isEqualTo(1L);
        assertThat(result.get(0).getUsernameUsuario()).isEqualTo("testuser");
        assertThat(result.get(0).getTipoActividad()).isEqualTo("LOGIN");
        assertThat(result.get(1).getIdLog()).isEqualTo(2L);
        assertThat(result.get(1).getTipoActividad()).isEqualTo("LOGOUT");

        verify(logActividadRepository).findAll();
        verify(usuarioClientRest, times(2)).findById(1L);
    }

    @Test
    @DisplayName("Debe manejar error de Feign al obtener usuario")
    void testFindAllConErrorFeign() {
        // Given
        when(logActividadRepository.findAll()).thenReturn(Arrays.asList(logActividad));
        when(usuarioClientRest.findById(anyLong())).thenThrow(mock(FeignException.class));

        // When
        List<LogActividadDTO> result = logActividadService.findAll();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsernameUsuario()).isEqualTo("Desconocido");
        assertThat(result.get(0).getIdUsuario()).isEqualTo(1L);

        verify(logActividadRepository).findAll();
        verify(usuarioClientRest).findById(1L);
    }

    @Test
    @DisplayName("Debe encontrar log por ID")
    void testFindById() {
        // Given
        when(logActividadRepository.findById(1L)).thenReturn(Optional.of(logActividad));

        // When
        LogActividad result = logActividadService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIdLog()).isEqualTo(1L);
        assertThat(result.getTipoActividad()).isEqualTo("LOGIN");

        verify(logActividadRepository).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando log no existe")
    void testFindByIdNotFound() {
        // Given
        when(logActividadRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> logActividadService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Log")
                .hasMessageContaining("999");

        verify(logActividadRepository).findById(999L);
    }

    @Test
    @DisplayName("Debe encontrar logs por usuario")
    void testFindByUsuario() {
        // Given
        LogActividad log1 = LogActividad.builder()
                .idLog(1L)
                .idUsuario(1L)
                .tipoActividad("LOGIN")
                .fecha(now)
                .resultado("EXITOSO")
                .build();

        LogActividad log2 = LogActividad.builder()
                .idLog(2L)
                .idUsuario(1L)
                .tipoActividad("VER_PRODUCTO")
                .fecha(now.plusMinutes(5))
                .resultado("EXITOSO")
                .build();

        when(logActividadRepository.findByIdUsuario(1L)).thenReturn(Arrays.asList(log1, log2));
        when(usuarioClientRest.findById(1L)).thenReturn(usuario);

        // When
        List<LogActividadDTO> result = logActividadService.findByUsuario(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIdUsuario()).isEqualTo(1L);
        assertThat(result.get(0).getUsernameUsuario()).isEqualTo("testuser");
        assertThat(result.get(1).getIdUsuario()).isEqualTo(1L);

        verify(logActividadRepository).findByIdUsuario(1L);
        verify(usuarioClientRest, times(2)).findById(1L);
    }

    @Test
    @DisplayName("Debe encontrar logs por tipo de actividad")
    void testFindByTipoActividad() {
        // Given
        LogActividad log1 = LogActividad.builder()
                .idLog(1L)
                .idUsuario(1L)
                .tipoActividad("LOGIN")
                .fecha(now)
                .resultado("EXITOSO")
                .build();

        LogActividad log2 = LogActividad.builder()
                .idLog(2L)
                .idUsuario(2L)
                .tipoActividad("LOGIN")
                .fecha(now.plusHours(1))
                .resultado("EXITOSO")
                .build();

        when(logActividadRepository.findByTipoActividad("LOGIN")).thenReturn(Arrays.asList(log1, log2));
        when(usuarioClientRest.findById(1L)).thenReturn(usuario);
        
        Usuario usuario2 = new Usuario();
        usuario2.setIdUsuario(2L);
        usuario2.setUsername("user2");
        when(usuarioClientRest.findById(2L)).thenReturn(usuario2);

        // When
        List<LogActividadDTO> result = logActividadService.findByTipoActividad("LOGIN");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTipoActividad()).isEqualTo("LOGIN");
        assertThat(result.get(1).getTipoActividad()).isEqualTo("LOGIN");

        verify(logActividadRepository).findByTipoActividad("LOGIN");
    }

    @Test
    @DisplayName("Debe encontrar logs por rango de fechas")
    void testFindByFechaRango() {
        // Given
        LocalDateTime fechaInicio = now.minusHours(2);
        LocalDateTime fechaFin = now.plusHours(2);

        LogActividad log1 = LogActividad.builder()
                .idLog(1L)
                .idUsuario(1L)
                .tipoActividad("LOGIN")
                .fecha(now)
                .resultado("EXITOSO")
                .build();

        when(logActividadRepository.findByFechaBetween(fechaInicio, fechaFin))
                .thenReturn(Arrays.asList(log1));
        when(usuarioClientRest.findById(1L)).thenReturn(usuario);

        // When
        List<LogActividadDTO> result = logActividadService.findByFechaRango(fechaInicio, fechaFin);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFecha()).isEqualTo(now);

        verify(logActividadRepository).findByFechaBetween(fechaInicio, fechaFin);
    }

    @Test
    @DisplayName("Debe encontrar logs por usuario y rango de fechas")
    void testFindByUsuarioYFecha() {
        // Given
        LocalDateTime fechaInicio = now.minusHours(1);
        LocalDateTime fechaFin = now.plusHours(1);

        LogActividad log1 = LogActividad.builder()
                .idLog(1L)
                .idUsuario(1L)
                .tipoActividad("LOGIN")
                .fecha(now)
                .resultado("EXITOSO")
                .build();

        when(logActividadRepository.findByIdUsuarioAndFechaBetween(1L, fechaInicio, fechaFin))
                .thenReturn(Arrays.asList(log1));
        when(usuarioClientRest.findById(1L)).thenReturn(usuario);

        // When
        List<LogActividadDTO> result = logActividadService.findByUsuarioYFecha(1L, fechaInicio, fechaFin);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdUsuario()).isEqualTo(1L);
        assertThat(result.get(0).getFecha()).isEqualTo(now);

        verify(logActividadRepository).findByIdUsuarioAndFechaBetween(1L, fechaInicio, fechaFin);
    }

    @Test
    @DisplayName("Debe encontrar logs por recurso")
    void testFindByRecurso() {
        // Given
        LogActividad log1 = LogActividad.builder()
                .idLog(1L)
                .idUsuario(1L)
                .tipoActividad("VER_PRODUCTO")
                .fecha(now)
                .idRecurso(100L)
                .tipoRecurso("PRODUCTO")
                .resultado("EXITOSO")
                .build();

        when(logActividadRepository.findByIdRecursoAndTipoRecurso(100L, "PRODUCTO"))
                .thenReturn(Arrays.asList(log1));
        when(usuarioClientRest.findById(1L)).thenReturn(usuario);

        // When
        List<LogActividadDTO> result = logActividadService.findByRecurso(100L, "PRODUCTO");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdRecurso()).isEqualTo(100L);
        assertThat(result.get(0).getTipoRecurso()).isEqualTo("PRODUCTO");

        verify(logActividadRepository).findByIdRecursoAndTipoRecurso(100L, "PRODUCTO");
    }

    @Test
    @DisplayName("Debe convertir log a DTO con usuario null")
    void testConvertirADTOConUsuarioNull() {
        // Given
        LogActividad logSinUsuario = LogActividad.builder()
                .idLog(1L)
                .idUsuario(null)
                .tipoActividad("SYSTEM")
                .fecha(now)
                .descripcion("Tarea del sistema")
                .resultado("EXITOSO")
                .build();

        when(logActividadRepository.findAll()).thenReturn(Arrays.asList(logSinUsuario));

        // When
        List<LogActividadDTO> result = logActividadService.findAll();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdUsuario()).isNull();
        assertThat(result.get(0).getUsernameUsuario()).isEqualTo("Desconocido");

        verify(logActividadRepository).findAll();
        verify(usuarioClientRest, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Debe registrar log con resultado fallido")
    void testRegistrarLogFallido() {
        // Given
        RegistrarLogDTO dto = RegistrarLogDTO.builder()
                .idUsuario(1L)
                .tipoActividad("LOGIN")
                .descripcion("Intento de login fallido")
                .ipAddress("192.168.1.1")
                .resultado("FALLIDO")
                .datosAdicionales("{\"reason\":\"invalid_credentials\"}")
                .build();

        LogActividad logFallido = LogActividad.builder()
                .idLog(1L)
                .idUsuario(1L)
                .tipoActividad("LOGIN")
                .fecha(now)
                .descripcion("Intento de login fallido")
                .ipAddress("192.168.1.1")
                .resultado("FALLIDO")
                .datosAdicionales("{\"reason\":\"invalid_credentials\"}")
                .build();

        when(logActividadRepository.save(any(LogActividad.class))).thenReturn(logFallido);

        // When
        LogActividad result = logActividadService.registrarLog(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResultado()).isEqualTo("FALLIDO");
        assertThat(result.getDatosAdicionales()).contains("invalid_credentials");

        verify(logActividadRepository).save(any(LogActividad.class));
    }

    @Test
    @DisplayName("Debe encontrar logs vacíos cuando no hay datos")
    void testFindAllVacio() {
        // Given
        when(logActividadRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<LogActividadDTO> result = logActividadService.findAll();

        // Then
        assertThat(result).isEmpty();

        verify(logActividadRepository).findAll();
        verify(usuarioClientRest, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Debe encontrar logs por usuario vacío cuando no hay datos")
    void testFindByUsuarioVacio() {
        // Given
        when(logActividadRepository.findByIdUsuario(999L)).thenReturn(Arrays.asList());

        // When
        List<LogActividadDTO> result = logActividadService.findByUsuario(999L);

        // Then
        assertThat(result).isEmpty();

        verify(logActividadRepository).findByIdUsuario(999L);
        verify(usuarioClientRest, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Debe registrar log de compra con todos los datos")
    void testRegistrarLogCompra() {
        // Given
        RegistrarLogDTO dto = RegistrarLogDTO.builder()
                .idUsuario(1L)
                .tipoActividad("COMPRA")
                .descripcion("Pedido realizado")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .idRecurso(500L)
                .tipoRecurso("PEDIDO")
                .resultado("EXITOSO")
                .datosAdicionales("{\"total\":150.50,\"items\":3}")
                .build();

        LogActividad logCompra = LogActividad.builder()
                .idLog(1L)
                .idUsuario(1L)
                .tipoActividad("COMPRA")
                .fecha(now)
                .descripcion("Pedido realizado")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .idRecurso(500L)
                .tipoRecurso("PEDIDO")
                .resultado("EXITOSO")
                .datosAdicionales("{\"total\":150.50,\"items\":3}")
                .build();

        when(logActividadRepository.save(any(LogActividad.class))).thenReturn(logCompra);

        // When
        LogActividad result = logActividadService.registrarLog(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTipoActividad()).isEqualTo("COMPRA");
        assertThat(result.getIdRecurso()).isEqualTo(500L);
        assertThat(result.getTipoRecurso()).isEqualTo("PEDIDO");
        assertThat(result.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(result.getDatosAdicionales()).contains("total");

        verify(logActividadRepository).save(any(LogActividad.class));
    }
}
