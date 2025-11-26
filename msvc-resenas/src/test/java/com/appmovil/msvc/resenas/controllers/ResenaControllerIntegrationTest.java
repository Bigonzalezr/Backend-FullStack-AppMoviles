package com.appmovil.msvc.resenas.controllers;

import com.appmovil.msvc.resenas.clients.ProductoClientRest;
import com.appmovil.msvc.resenas.clients.UsuarioClientRest;
import com.appmovil.msvc.resenas.models.Producto;
import com.appmovil.msvc.resenas.models.Usuario;
import com.appmovil.msvc.resenas.models.entities.Resena;
import com.appmovil.msvc.resenas.repositories.ResenaRepository;
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
@DisplayName("ResenaController - Integration Tests")
class ResenaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResenaRepository ResenaRepository;

 
    private UsuarioClientRest usuarioClientRest;

   
    private ProductoClientRest productoClientRest;

    private Usuario usuarioTest;
    private Producto productoTest;
    private Resena ResenaTest;

    @BeforeEach
    void setUp() {
        ResenaRepository.deleteAll();

        usuarioTest = new Usuario();
        usuarioTest.setIdUsuario(1L);
        usuarioTest.setNombre("Carlos Mendoza");
        usuarioTest.setActivo(true);

        productoTest = new Producto();
        productoTest.setIdProducto(1L);
        productoTest.setNombre("Samsung Galaxy S23");
        productoTest.setActivo(true);

        ResenaTest = new Resena();
        ResenaTest.setIdUsuario(1L);
        ResenaTest.setIdProducto(1L);
        ResenaTest.setRating(5);
        ResenaTest.setComentario("Excelente teléfono, muy buena cámara y rendimiento");
        ResenaTest.setFechaCreacion(LocalDateTime.now());
        ResenaTest.setActivo(true);

        when(usuarioClientRest.findById(anyLong())).thenReturn(usuarioTest);
        when(productoClientRest.findById(anyLong())).thenReturn(productoTest);
    }

    @Test
    @DisplayName("POST /api/resenas - Debe crear Resena exitosamente")
    void testCrearResena_ConDatosValidos_DebeRetornar200() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/resenas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ResenaTest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.idProducto").value(1))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comentario").value(containsString("Excelente")))
                .andExpect(jsonPath("$.nombreUsuario").value("Carlos Mendoza"))
                .andExpect(jsonPath("$.nombreProducto").value("Samsung Galaxy S23"));

        assertThat(ResenaRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("POST /api/resenas - Debe retornar 400 con campos inválidos")
    void testCrearResena_ConCamposInvalidos_DebeRetornar400() throws Exception {
        // Given
        ResenaTest.setRating(6); // Rating inválido (máximo 5)
        ResenaTest.setComentario("Corto"); // Comentario muy corto (mínimo 10)

        // When & Then
        mockMvc.perform(post("/api/resenas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ResenaTest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/resenas - Debe retornar 400 con Resena duplicada")
    void testCrearResena_Duplicada_DebeRetornar400() throws Exception {
        // Given - Crear primera Resena
        ResenaRepository.save(ResenaTest);

        // When & Then - Intentar crear segunda Resena del mismo usuario/producto
        Resena duplicada = new Resena();
        duplicada.setIdUsuario(1L);
        duplicada.setIdProducto(1L);
        duplicada.setRating(4);
        duplicada.setComentario("Segunda Resena que no debería permitirse");

        mockMvc.perform(post("/api/resenas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicada)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Ya existe una Resena")));
    }

    @Test
    @DisplayName("GET /api/resenas/{id} - Debe retornar Resena por ID")
    void testFindById_ConIdValido_DebeRetornarResena() throws Exception {
        // Given
        Resena guardada = ResenaRepository.save(ResenaTest);

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
    @DisplayName("PUT /api/resenas/{id} - Debe actualizar Resena")
    void testUpdate_ConDatosValidos_DebeActualizar() throws Exception {
        // Given
        Resena guardada = ResenaRepository.save(ResenaTest);

        Resena actualizada = new Resena();
        actualizada.setRating(4);
        actualizada.setComentario("Actualizando mi opinión: Buen producto pero no perfecto");

        // When & Then
        mockMvc.perform(put("/api/resenas/{id}", guardada.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comentario", containsString("Actualizando")));

        Resena enDB = ResenaRepository.findById(guardada.getId()).orElseThrow();
        assertThat(enDB.getRating()).isEqualTo(4);
    }

    @Test
    @DisplayName("DELETE /api/resenas/{id} - Debe desactivar Resena")
    void testDelete_DebeDesactivarResena() throws Exception {
        // Given
        Resena guardada = ResenaRepository.save(ResenaTest);

        // When & Then
        mockMvc.perform(delete("/api/resenas/{id}", guardada.getId()))
                .andExpect(status().isNoContent());

        Resena enDB = ResenaRepository.findById(guardada.getId()).orElseThrow();
        assertThat(enDB.getActivo()).isFalse();
    }

    @Test
    @DisplayName("GET /api/resenas/usuario/{idUsuario} - Debe retornar Resenas del usuario")
    void testFindByUsuario_DebeRetornarResenasDelUsuario() throws Exception {
        // Given
        ResenaRepository.save(ResenaTest);

        // When & Then
        mockMvc.perform(get("/api/resenas/usuario/{idUsuario}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idUsuario").value(1));
    }

    @Test
    @DisplayName("GET /api/resenas/producto/{idProducto} - Debe retornar solo Resenas activas")
    void testFindByProducto_DebeRetornarSoloActivas() throws Exception {
        // Given
        ResenaRepository.save(ResenaTest);

        Resena inactiva = new Resena();
        inactiva.setIdUsuario(2L);
        inactiva.setIdProducto(1L);
        inactiva.setRating(3);
        inactiva.setComentario("Esta Resena está desactivada por algún motivo");
        inactiva.setActivo(false);
        ResenaRepository.save(inactiva);

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
        ResenaRepository.save(ResenaTest); // Rating 5

        Resena Resena2 = new Resena();
        Resena2.setIdUsuario(2L);
        Resena2.setIdProducto(1L);
        Resena2.setRating(3);
        Resena2.setComentario("Es bueno pero le falta algo, esperaba más");
        Resena2.setActivo(true);
        ResenaRepository.save(Resena2); // Rating 3

        // When & Then
        mockMvc.perform(get("/api/resenas/producto/{idProducto}/promedio", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(4.0)); // Promedio (5 + 3) / 2 = 4.0
    }

    @Test
    @DisplayName("GET /api/resenas/producto/{idProducto}/promedio - Debe retornar 0 sin Resenas")
    void testGetAverageRating_SinResenas_DebeRetornar0() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/resenas/producto/{idProducto}/promedio", 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0.0));
    }

    @Test
    @DisplayName("Flujo completo - Crear, consultar, actualizar y eliminar Resena")
    void testFlujoCompletoResena() throws Exception {
        // 1. Crear Resena
        String response = mockMvc.perform(post("/api/resenas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ResenaTest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long idResena = objectMapper.readTree(response).get("id").asLong();

        // 2. Consultar Resena creada
        mockMvc.perform(get("/api/resenas/{id}", idResena))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5));

        // 3. Actualizar Resena
        Resena actualizada = new Resena();
        actualizada.setRating(4);
        actualizada.setComentario("Cambiando mi opinión después de usarlo más tiempo");

        mockMvc.perform(put("/api/resenas/{id}", idResena)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4));

        // 4. Verificar promedio
        mockMvc.perform(get("/api/resenas/producto/{idProducto}/promedio", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(4.0));

        // 5. Eliminar Resena (soft delete)
        mockMvc.perform(delete("/api/resenas/{id}", idResena))
                .andExpect(status().isNoContent());

        // Verificar que existe pero está inactiva
        Resena enDB = ResenaRepository.findById(idResena).orElseThrow();
        assertThat(enDB.getActivo()).isFalse();
    }
}
