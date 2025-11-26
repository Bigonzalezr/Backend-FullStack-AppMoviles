package com.appmovil.msvc.resenas.controllers;

import com.appmovil.msvc.resenas.clients.ProductoClientRest;
import com.appmovil.msvc.resenas.clients.UsuarioClientRest;
import com.appmovil.msvc.resenas.models.Producto;
import com.appmovil.msvc.resenas.models.Usuario;
import com.appmovil.msvc.resenas.models.entities.Reseña;
import com.appmovil.msvc.resenas.repositories.ReseñaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ReseñaController - Integration Tests")
class ReseñaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReseñaRepository reseñaRepository;

    @MockBean
    private UsuarioClientRest usuarioClientRest;

    @MockBean
    private ProductoClientRest productoClientRest;

    private Usuario usuarioTest;
    private Producto productoTest;
    private Reseña reseñaTest;

    @BeforeEach
    void setUp() {
        reseñaRepository.deleteAll();

        usuarioTest = new Usuario();
        usuarioTest.setIdUsuario(1L);
        usuarioTest.setNombre("Carlos Mendoza");
        usuarioTest.setActivo(true);

        productoTest = new Producto();
        productoTest.setIdProducto(1L);
        productoTest.setNombre("Samsung Galaxy S23");
        productoTest.setActivo(true);

        reseñaTest = new Reseña();
        reseñaTest.setIdUsuario(1L);
        reseñaTest.setIdProducto(1L);
        reseñaTest.setRating(5);
        reseñaTest.setComentario("Excelente teléfono, muy buena cámara y rendimiento");
        reseñaTest.setFechaCreacion(LocalDateTime.now());
        reseñaTest.setActivo(true);

        when(usuarioClientRest.findById(anyLong())).thenReturn(usuarioTest);
        when(productoClientRest.findById(anyLong())).thenReturn(productoTest);
    }

    @Test
    @DisplayName("POST /api/resenas - Debe crear reseña exitosamente")
    void testCrearReseña_ConDatosValidos_DebeRetornar200() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/resenas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reseñaTest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.idProducto").value(1))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comentario").value(containsString("Excelente")))
                .andExpect(jsonPath("$.nombreUsuario").value("Carlos Mendoza"))
                .andExpect(jsonPath("$.nombreProducto").value("Samsung Galaxy S23"));

        assertThat(reseñaRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("POST /api/resenas - Debe retornar 400 con campos inválidos")
    void testCrearReseña_ConCamposInvalidos_DebeRetornar400() throws Exception {
        // Given
        reseñaTest.setRating(6); // Rating inválido (máximo 5)
        reseñaTest.setComentario("Corto"); // Comentario muy corto (mínimo 10)

        // When & Then
        mockMvc.perform(post("/api/resenas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reseñaTest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/resenas - Debe retornar 400 con reseña duplicada")
    void testCrearReseña_Duplicada_DebeRetornar400() throws Exception {
        // Given - Crear primera reseña
        reseñaRepository.save(reseñaTest);

        // When & Then - Intentar crear segunda reseña del mismo usuario/producto
        Reseña duplicada = new Reseña();
        duplicada.setIdUsuario(1L);
        duplicada.setIdProducto(1L);
        duplicada.setRating(4);
        duplicada.setComentario("Segunda reseña que no debería permitirse");

        mockMvc.perform(post("/api/resenas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicada)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Ya existe una reseña")));
    }

    @Test
    @DisplayName("GET /api/resenas/{id} - Debe retornar reseña por ID")
    void testFindById_ConIdValido_DebeRetornarReseña() throws Exception {
        // Given
        Reseña guardada = reseñaRepository.save(reseñaTest);

        // When & Then
        mockMvc.perform(get("/api/resenas/{id}", guardada.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardada.getId()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.nombreUsuario").value("Carlos Mendoza"));
    }

    @Test
    @DisplayName("GET /api/resenas/{id} - Debe retornar 404 con ID inexistente")
    void testFindById_ConIdInexistente_DebeRetornar404() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/resenas/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/resenas/{id} - Debe actualizar reseña")
    void testUpdate_ConDatosValidos_DebeActualizar() throws Exception {
        // Given
        Reseña guardada = reseñaRepository.save(reseñaTest);

        Reseña actualizada = new Reseña();
        actualizada.setRating(4);
        actualizada.setComentario("Actualizando mi opinión: Buen producto pero no perfecto");

        // When & Then
        mockMvc.perform(put("/api/resenas/{id}", guardada.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comentario", containsString("Actualizando")));

        Reseña enDB = reseñaRepository.findById(guardada.getId()).orElseThrow();
        assertThat(enDB.getRating()).isEqualTo(4);
    }

    @Test
    @DisplayName("DELETE /api/resenas/{id} - Debe desactivar reseña")
    void testDelete_DebeDesactivarReseña() throws Exception {
        // Given
        Reseña guardada = reseñaRepository.save(reseñaTest);

        // When & Then
        mockMvc.perform(delete("/api/resenas/{id}", guardada.getId()))
                .andExpect(status().isNoContent());

        Reseña enDB = reseñaRepository.findById(guardada.getId()).orElseThrow();
        assertThat(enDB.getActivo()).isFalse();
    }

    @Test
    @DisplayName("GET /api/resenas/usuario/{idUsuario} - Debe retornar reseñas del usuario")
    void testFindByUsuario_DebeRetornarReseñasDelUsuario() throws Exception {
        // Given
        reseñaRepository.save(reseñaTest);

        // When & Then
        mockMvc.perform(get("/api/resenas/usuario/{idUsuario}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idUsuario").value(1));
    }

    @Test
    @DisplayName("GET /api/resenas/producto/{idProducto} - Debe retornar solo reseñas activas")
    void testFindByProducto_DebeRetornarSoloActivas() throws Exception {
        // Given
        reseñaRepository.save(reseñaTest);

        Reseña inactiva = new Reseña();
        inactiva.setIdUsuario(2L);
        inactiva.setIdProducto(1L);
        inactiva.setRating(3);
        inactiva.setComentario("Esta reseña está desactivada por algún motivo");
        inactiva.setActivo(false);
        reseñaRepository.save(inactiva);

        // When & Then
        mockMvc.perform(get("/api/resenas/producto/{idProducto}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    @DisplayName("GET /api/resenas/producto/{idProducto}/promedio - Debe calcular promedio")
    void testGetAverageRating_DebeCalcularPromedio() throws Exception {
        // Given
        reseñaRepository.save(reseñaTest); // Rating 5

        Reseña reseña2 = new Reseña();
        reseña2.setIdUsuario(2L);
        reseña2.setIdProducto(1L);
        reseña2.setRating(3);
        reseña2.setComentario("Es bueno pero le falta algo, esperaba más");
        reseña2.setActivo(true);
        reseñaRepository.save(reseña2); // Rating 3

        // When & Then
        mockMvc.perform(get("/api/resenas/producto/{idProducto}/promedio", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(4.0)); // Promedio (5 + 3) / 2 = 4.0
    }

    @Test
    @DisplayName("GET /api/resenas/producto/{idProducto}/promedio - Debe retornar 0 sin reseñas")
    void testGetAverageRating_SinReseñas_DebeRetornar0() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/resenas/producto/{idProducto}/promedio", 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0.0));
    }

    @Test
    @DisplayName("Flujo completo - Crear, consultar, actualizar y eliminar reseña")
    void testFlujoCompletoReseña() throws Exception {
        // 1. Crear reseña
        String response = mockMvc.perform(post("/api/resenas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reseñaTest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long idReseña = objectMapper.readTree(response).get("id").asLong();

        // 2. Consultar reseña creada
        mockMvc.perform(get("/api/resenas/{id}", idReseña))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5));

        // 3. Actualizar reseña
        Reseña actualizada = new Reseña();
        actualizada.setRating(4);
        actualizada.setComentario("Cambiando mi opinión después de usarlo más tiempo");

        mockMvc.perform(put("/api/resenas/{id}", idReseña)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4));

        // 4. Verificar promedio
        mockMvc.perform(get("/api/resenas/producto/{idProducto}/promedio", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(4.0));

        // 5. Eliminar reseña (soft delete)
        mockMvc.perform(delete("/api/resenas/{id}", idReseña))
                .andExpect(status().isNoContent());

        // Verificar que existe pero está inactiva
        Reseña enDB = reseñaRepository.findById(idReseña).orElseThrow();
        assertThat(enDB.getActivo()).isFalse();
    }
}
