package com.appmovil.msvc.logs.controller;

import com.appmovil.msvc.logs.dtos.LogActividadDTO;
import com.appmovil.msvc.logs.dtos.RegistrarLogDTO;
import com.appmovil.msvc.logs.models.entities.LogActividad;
import com.appmovil.msvc.logs.repositories.LogActividadRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LogActividadControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogActividadRepository logActividadRepository;

    private DateTimeFormatter formatter;

    @BeforeEach
    void setUp() {
        logActividadRepository.deleteAll();
        formatter = DateTimeFormatter.ISO_DATE_TIME;
    }

    @Test
    @DisplayName("POST /api/v1/logs - Debe registrar un nuevo log exitosamente")
    void testRegistrarLog() throws Exception {
        // Given
        RegistrarLogDTO dto = RegistrarLogDTO.builder()
                .idUsuario(1L)
                .tipoActividad("LOGIN")
                .descripcion("Usuario inició sesión exitosamente")
                .ipAddress("192.168.1.100")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .resultado("EXITOSO")
                .datosAdicionales("{\"device\":\"desktop\"}")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idLog").exists())
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.tipoActividad").value("LOGIN"))
                .andExpect(jsonPath("$.descripcion").value("Usuario inició sesión exitosamente"))
                .andExpect(jsonPath("$.ipAddress").value("192.168.1.100"))
                .andExpect(jsonPath("$.resultado").value("EXITOSO"));

        // Verify in database
        List<LogActividad> logs = logActividadRepository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getTipoActividad()).isEqualTo("LOGIN");
        assertThat(logs.get(0).getDescripcion()).contains("exitosamente");
    }

    @Test
    @DisplayName("POST /api/v1/logs - Debe validar campos requeridos")
    void testRegistrarLogValidacion() throws Exception {
        // Given - DTO sin tipoActividad (campo requerido)
        RegistrarLogDTO dto = RegistrarLogDTO.builder()
                .idUsuario(1L)
                .descripcion("Test log")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/logs - Debe registrar log con datos de recurso")
    void testRegistrarLogConRecurso() throws Exception {
        // Given
        RegistrarLogDTO dto = RegistrarLogDTO.builder()
                .idUsuario(1L)
                .tipoActividad("VER_PRODUCTO")
                .descripcion("Usuario visualizó el producto")
                .ipAddress("192.168.1.100")
                .idRecurso(100L)
                .tipoRecurso("PRODUCTO")
                .resultado("EXITOSO")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idRecurso").value(100))
                .andExpect(jsonPath("$.tipoRecurso").value("PRODUCTO"));

        // Verify
        List<LogActividad> logs = logActividadRepository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getIdRecurso()).isEqualTo(100L);
        assertThat(logs.get(0).getTipoRecurso()).isEqualTo("PRODUCTO");
    }

    @Test
    @DisplayName("GET /api/v1/logs - Debe obtener todos los logs")
    void testFindAll() throws Exception {
        // Given
        LogActividad log1 = LogActividad.builder()
                .idUsuario(1L)
                .tipoActividad("LOGIN")
                .fecha(LocalDateTime.now())
                .descripcion("Login exitoso")
                .resultado("EXITOSO")
                .build();

        LogActividad log2 = LogActividad.builder()
                .idUsuario(1L)
                .tipoActividad("LOGOUT")
                .fecha(LocalDateTime.now())
                .descripcion("Logout")
                .resultado("EXITOSO")
                .build();

        logActividadRepository.saveAll(List.of(log1, log2));

        // When & Then
        mockMvc.perform(get("/api/v1/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tipoActividad", is(oneOf("LOGIN", "LOGOUT"))))
                .andExpect(jsonPath("$[1].tipoActividad", is(oneOf("LOGIN", "LOGOUT"))));
    }

    @Test
    @DisplayName("GET /api/v1/logs/{id} - Debe obtener log por ID")
    void testFindById() throws Exception {
        // Given
        LogActividad log = LogActividad.builder()
                .idUsuario(1L)
                .tipoActividad("LOGIN")
                .fecha(LocalDateTime.now())
                .descripcion("Login exitoso")
                .ipAddress("192.168.1.1")
                .resultado("EXITOSO")
                .build();

        LogActividad savedLog = logActividadRepository.save(log);

        // When & Then
        mockMvc.perform(get("/api/v1/logs/{id}", savedLog.getIdLog()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idLog").value(savedLog.getIdLog()))
                .andExpect(jsonPath("$.tipoActividad").value("LOGIN"))
                .andExpect(jsonPath("$.descripcion").value("Login exitoso"));
    }

    @Test
    @DisplayName("GET /api/v1/logs/{id} - Debe retornar 404 cuando log no existe")
    void testFindByIdNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/logs/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/logs/usuario/{idUsuario} - Debe obtener logs por usuario")
    void testFindByUsuario() throws Exception {
        // Given
        LogActividad log1 = LogActividad.builder()
                .idUsuario(1L)
                .tipoActividad("LOGIN")
                .fecha(LocalDateTime.now())
                .descripcion("Login")
                .resultado("EXITOSO")
                .build();

        LogActividad log2 = LogActividad.builder()
                .idUsuario(1L)
                .tipoActividad("VER_PRODUCTO")
                .fecha(LocalDateTime.now())
                .descripcion("Ver producto")
                .resultado("EXITOSO")
                .build();

        LogActividad log3 = LogActividad.builder()
                .idUsuario(2L)
                .tipoActividad("LOGIN")
                .fecha(LocalDateTime.now())
                .descripcion("Login otro usuario")
                .resultado("EXITOSO")
                .build();

        logActividadRepository.saveAll(List.of(log1, log2, log3));

        // When & Then
        mockMvc.perform(get("/api/v1/logs/usuario/{idUsuario}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].idUsuario").value(1))
                .andExpect(jsonPath("$[1].idUsuario").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/logs/tipo/{tipoActividad} - Debe obtener logs por tipo de actividad")
    void testFindByTipoActividad() throws Exception {
        // Given
        LogActividad log1 = LogActividad.builder()
                .idUsuario(1L)
                .tipoActividad("LOGIN")
                .fecha(LocalDateTime.now())
                .descripcion("Login usuario 1")
                .resultado("EXITOSO")
                .build();

        LogActividad log2 = LogActividad.builder()
                .idUsuario(2L)
                .tipoActividad("LOGIN")
                .fecha(LocalDateTime.now())
                .descripcion("Login usuario 2")
                .resultado("EXITOSO")
                .build();

        LogActividad log3 = LogActividad.builder()
                .idUsuario(1L)
                .tipoActividad("LOGOUT")
                .fecha(LocalDateTime.now())
                .descripcion("Logout")
                .resultado("EXITOSO")
                .build();

        logActividadRepository.saveAll(List.of(log1, log2, log3));

        // When & Then
        mockMvc.perform(get("/api/v1/logs/tipo/{tipoActividad}", "LOGIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tipoActividad").value("LOGIN"))
                .andExpect(jsonPath("$[1].tipoActividad").value("LOGIN"));
    }

    @Test
    @DisplayName("GET /api/v1/logs/fecha-rango - Debe obtener logs por rango de fechas")
    void testFindByFechaRango() throws Exception {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime tomorrow = now.plusDays(1);

        LogActividad log1 = LogActividad.builder()
                .idUsuario(1L)
                .tipoActividad("LOGIN")
                .fecha(now)
                .descripcion("Login hoy")
                .resultado("EXITOSO")
                .build();

        LogActividad log2 = LogActividad.builder()
                .idUsuario(1L)
                .tipoActividad("LOGIN")
                .fecha(yesterday.minusDays(1)) // Hace 2 días, fuera del rango
                .descripcion("Login hace 2 días")
                .resultado("EXITOSO")
                .build();

        logActividadRepository.saveAll(List.of(log1, log2));

        // When & Then
        mockMvc.perform(get("/api/v1/logs/fecha-rango")
                        .param("fechaInicio", yesterday.format(formatter))
                        .param("fechaFin", tomorrow.format(formatter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descripcion").value("Login hoy"));
    }

    @Test
    @DisplayName("GET /api/v1/logs/usuario/{id}/fecha-rango - Debe obtener logs por usuario y fecha")
    void testFindByUsuarioYFecha() throws Exception {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime tomorrow = now.plusDays(1);

        LogActividad log1 = LogActividad.builder()
                .idUsuario(1L)
                .tipoActividad("LOGIN")
                .fecha(now)
                .descripcion("Login usuario 1 hoy")
                .resultado("EXITOSO")
                .build();

        LogActividad log2 = LogActividad.builder()
                .idUsuario(2L)
                .tipoActividad("LOGIN")
                .fecha(now)
                .descripcion("Login usuario 2 hoy")
                .resultado("EXITOSO")
                .build();

        LogActividad log3 = LogActividad.builder()
                .idUsuario(1L)
                .tipoActividad("LOGIN")
                .fecha(yesterday.minusDays(2)) // Fuera del rango
                .descripcion("Login usuario 1 hace días")
                .resultado("EXITOSO")
                .build();

        logActividadRepository.saveAll(List.of(log1, log2, log3));

        // When & Then
        mockMvc.perform(get("/api/v1/logs/usuario/{idUsuario}/fecha-rango", 1L)
                        .param("fechaInicio", yesterday.format(formatter))
                        .param("fechaFin", tomorrow.format(formatter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idUsuario").value(1))
                .andExpect(jsonPath("$[0].descripcion").value("Login usuario 1 hoy"));
    }

    @Test
    @DisplayName("GET /api/v1/logs/recurso - Debe obtener logs por recurso")
    void testFindByRecurso() throws Exception {
        // Given
        LogActividad log1 = LogActividad.builder()
                .idUsuario(1L)
                .tipoActividad("VER_PRODUCTO")
                .fecha(LocalDateTime.now())
                .descripcion("Ver producto 100")
                .idRecurso(100L)
                .tipoRecurso("PRODUCTO")
                .resultado("EXITOSO")
                .build();

        LogActividad log2 = LogActividad.builder()
                .idUsuario(1L)
                .tipoActividad("VER_PRODUCTO")
                .fecha(LocalDateTime.now())
                .descripcion("Ver producto 200")
                .idRecurso(200L)
                .tipoRecurso("PRODUCTO")
                .resultado("EXITOSO")
                .build();

        LogActividad log3 = LogActividad.builder()
                .idUsuario(1L)
                .tipoActividad("VER_PEDIDO")
                .fecha(LocalDateTime.now())
                .descripcion("Ver pedido 100")
                .idRecurso(100L)
                .tipoRecurso("PEDIDO")
                .resultado("EXITOSO")
                .build();

        logActividadRepository.saveAll(List.of(log1, log2, log3));

        // When & Then
        mockMvc.perform(get("/api/v1/logs/recurso")
                        .param("idRecurso", "100")
                        .param("tipoRecurso", "PRODUCTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idRecurso").value(100))
                .andExpect(jsonPath("$[0].tipoRecurso").value("PRODUCTO"))
                .andExpect(jsonPath("$[0].descripcion").value("Ver producto 100"));
    }

    @Test
    @DisplayName("Flujo completo - Registrar múltiples logs y consultar")
    void testFlujoCompletoLogs() throws Exception {
        // Given - Registrar varios logs
        RegistrarLogDTO loginDto = RegistrarLogDTO.builder()
                .idUsuario(1L)
                .tipoActividad("LOGIN")
                .descripcion("Usuario inició sesión")
                .ipAddress("192.168.1.100")
                .resultado("EXITOSO")
                .build();

        RegistrarLogDTO verProductoDto = RegistrarLogDTO.builder()
                .idUsuario(1L)
                .tipoActividad("VER_PRODUCTO")
                .descripcion("Usuario vio producto")
                .ipAddress("192.168.1.100")
                .idRecurso(50L)
                .tipoRecurso("PRODUCTO")
                .resultado("EXITOSO")
                .build();

        RegistrarLogDTO compraDto = RegistrarLogDTO.builder()
                .idUsuario(1L)
                .tipoActividad("COMPRA")
                .descripcion("Usuario realizó compra")
                .ipAddress("192.168.1.100")
                .idRecurso(200L)
                .tipoRecurso("PEDIDO")
                .resultado("EXITOSO")
                .datosAdicionales("{\"total\":250.00}")
                .build();

        // When - Registrar logs
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verProductoDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(compraDto)))
                .andExpect(status().isCreated());

        // Then - Verificar que todos los logs están registrados
        mockMvc.perform(get("/api/v1/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        // Verificar logs por usuario
        mockMvc.perform(get("/api/v1/logs/usuario/{idUsuario}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        // Verificar log por tipo
        mockMvc.perform(get("/api/v1/logs/tipo/{tipoActividad}", "LOGIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipoActividad").value("LOGIN"));

        // Verificar logs por recurso
        mockMvc.perform(get("/api/v1/logs/recurso")
                        .param("idRecurso", "50")
                        .param("tipoRecurso", "PRODUCTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descripcion").value("Usuario vio producto"));

        // Verify in database
        List<LogActividad> logs = logActividadRepository.findAll();
        assertThat(logs).hasSize(3);
        assertThat(logs).extracting(LogActividad::getTipoActividad)
                .containsExactlyInAnyOrder("LOGIN", "VER_PRODUCTO", "COMPRA");
    }
}
