package com.appmovil.msvc.productos.controller;

import com.appmovil.msvc.productos.dtos.ProductoUpdateDTO;
import com.appmovil.msvc.productos.models.entities.Producto;
import com.appmovil.msvc.productos.repositories.ProductoRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ProductoController - Integration Tests")
class ProductoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductoRepository productoRepository;

    private Producto productoTest;

    @BeforeEach
    void setUp() {
        productoRepository.deleteAll();

        productoTest = Producto.builder()
                .nombre("iPhone 15 Pro")
                .descripcion("Smartphone Apple última generación")
                .precio(1199)
                .stock(25)
                .categoria("SMARTPHONES")
                .activo(true)
                .rating(4.8)
                .build();
    }

    @Test
    @DisplayName("POST /api/productos - Debe crear producto exitosamente")
    void testCrearProducto_ConDatosValidos_DebeRetornar200() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoTest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("iPhone 15 Pro"))
                .andExpect(jsonPath("$.precio").value(1199))
                .andExpect(jsonPath("$.stock").value(25))
                .andExpect(jsonPath("$.categoria").value("SMARTPHONES"))
                .andExpect(jsonPath("$.rating").value(4.8))
                .andExpect(jsonPath("$.activo").value(true));

        // Verificar en DB
        assertThat(productoRepository.findByNombreContainingIgnoreCase("iPhone")).hasSize(1);
    }

    @Test
    @DisplayName("POST /api/productos - Debe retornar 400 con campos inválidos")
    void testCrearProducto_ConCamposInvalidos_DebeRetornar400() throws Exception {
        // Given
        Producto productoInvalido = Producto.builder()
                .nombre("") // Nombre vacío
                .precio(null) // Precio null
                .build();

        // When & Then
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/productos - Debe retornar lista de productos")
    void testFindAll_DebeRetornarLista() throws Exception {
        // Given
        productoRepository.save(productoTest);

        // When & Then
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].nombre").value("iPhone 15 Pro"));
    }

    @Test
    @DisplayName("GET /api/productos/{id} - Debe retornar producto por ID")
    void testFindById_ConIdValido_DebeRetornarProducto() throws Exception {
        // Given
        Producto guardado = productoRepository.save(productoTest);

        // When & Then
        mockMvc.perform(get("/api/productos/{id}", guardado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardado.getId()))
                .andExpect(jsonPath("$.nombre").value("iPhone 15 Pro"));
    }

    @Test
    @DisplayName("GET /api/productos/{id} - Debe retornar 404 con ID inexistente")
    void testFindById_ConIdInexistente_DebeRetornar404() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/productos/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/productos/{id} - Debe actualizar producto")
    void testUpdate_ConDatosValidos_DebeActualizar() throws Exception {
        // Given
        Producto guardado = productoRepository.save(productoTest);

        ProductoUpdateDTO updateDTO = new ProductoUpdateDTO();
        updateDTO.setNombre("iPhone 15 Pro Max");
        updateDTO.setPrecio(1399);
        updateDTO.setStock(30);
        updateDTO.setCategoria("SMARTPHONES");
        updateDTO.setDescripcion("Versión Max");
        updateDTO.setRating(4.9);

        // When & Then
        mockMvc.perform(put("/api/productos/{id}", guardado.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("iPhone 15 Pro Max"))
                .andExpect(jsonPath("$.precio").value(1399))
                .andExpect(jsonPath("$.rating").value(4.9));

        // Verificar en DB
        Producto actualizado = productoRepository.findById(guardado.getId()).orElseThrow();
        assertThat(actualizado.getNombre()).isEqualTo("iPhone 15 Pro Max");
        assertThat(actualizado.getPrecio()).isEqualTo(1399);
    }

    @Test
    @DisplayName("PUT /api/productos/{id} - Debe retornar 404 si producto no existe")
    void testUpdate_ConIdInexistente_DebeRetornar404() throws Exception {
        // Given
        ProductoUpdateDTO updateDTO = new ProductoUpdateDTO();
        updateDTO.setNombre("Producto");
        updateDTO.setPrecio(1000);
        updateDTO.setCategoria("CATEGORIA");
        updateDTO.setStock(10);

        // When & Then
        mockMvc.perform(put("/api/productos/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/productos/{id} - Debe eliminar producto")
    void testDelete_DebeEliminarExitosamente() throws Exception {
        // Given
        Producto guardado = productoRepository.save(productoTest);

        // When & Then
        mockMvc.perform(delete("/api/productos/{id}", guardado.getId()))
                .andExpect(status().isNoContent());

        // Verificar que fue eliminado
        assertThat(productoRepository.findById(guardado.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/productos/{id} - Debe retornar 404 si producto no existe")
    void testDelete_ConIdInexistente_DebeRetornar404() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/productos/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/productos/activos - Debe retornar solo productos activos")
    void testFindActivos_DebeRetornarSoloActivos() throws Exception {
        // Given
        productoRepository.save(productoTest);
        
        Producto inactivo = Producto.builder()
                .nombre("Producto Inactivo")
                .precio(500)
                .categoria("TEST")
                .stock(5)
                .activo(false)
                .build();
        productoRepository.save(inactivo);

        // When & Then
        mockMvc.perform(get("/api/productos/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    @DisplayName("GET /api/productos/categoria/{categoria} - Debe filtrar por categoría")
    void testFindByCategoria_DebeFiltrarPorCategoria() throws Exception {
        // Given
        productoRepository.save(productoTest);
        
        Producto otroProducto = Producto.builder()
                .nombre("Samsung Galaxy")
                .precio(900)
                .categoria("SMARTPHONES")
                .stock(15)
                .activo(true)
                .build();
        productoRepository.save(otroProducto);

        // When & Then
        mockMvc.perform(get("/api/productos/categoria/{categoria}", "SMARTPHONES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].categoria", everyItem(is("SMARTPHONES"))));
    }

    @Test
    @DisplayName("GET /api/productos/categoria/todos - Debe retornar todos los activos")
    void testFindByCategoria_Todos_DebeRetornarTodosActivos() throws Exception {
        // Given
        productoRepository.save(productoTest);

        // When & Then
        mockMvc.perform(get("/api/productos/categoria/{categoria}", "todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/productos/buscar?nombre={nombre} - Debe buscar por nombre")
    void testBuscarPorNombre_DebeRetornarCoincidencias() throws Exception {
        // Given
        productoRepository.save(productoTest);

        // When & Then
        mockMvc.perform(get("/api/productos/buscar")
                .param("nombre", "iPhone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].nombre", containsString("iPhone")));
    }

    @Test
    @DisplayName("PATCH /api/productos/{id}/stock - Debe actualizar stock")
    void testActualizarStock_DebeActualizarExitosamente() throws Exception {
        // Given
        Producto guardado = productoRepository.save(productoTest);

        // When & Then
        mockMvc.perform(patch("/api/productos/{id}/stock", guardado.getId())
                .param("cantidad", "-5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(20)); // 25 - 5

        // Verificar en DB
        Producto actualizado = productoRepository.findById(guardado.getId()).orElseThrow();
        assertThat(actualizado.getStock()).isEqualTo(20);
    }

    @Test
    @DisplayName("PATCH /api/productos/{id}/stock - Debe incrementar stock")
    void testActualizarStock_Incrementar_DebeAumentar() throws Exception {
        // Given
        Producto guardado = productoRepository.save(productoTest);

        // When & Then
        mockMvc.perform(patch("/api/productos/{id}/stock", guardado.getId())
                .param("cantidad", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(35)); // 25 + 10
    }

    @Test
    @DisplayName("PATCH /api/productos/{id}/stock - Debe retornar 400 con stock negativo")
    void testActualizarStock_StockNegativo_DebeRetornar400() throws Exception {
        // Given
        Producto guardado = productoRepository.save(productoTest);

        // When & Then
        mockMvc.perform(patch("/api/productos/{id}/stock", guardado.getId())
                .param("cantidad", "-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Stock insuficiente")));
    }

    @Test
    @DisplayName("Flujo completo - Crear, consultar, actualizar, buscar y eliminar")
    void testFlujoCompletoProducto() throws Exception {
        // 1. Crear producto
        String response = mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoTest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long idProducto = objectMapper.readTree(response).get("id").asLong();

        // 2. Consultar producto creado
        mockMvc.perform(get("/api/productos/{id}", idProducto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("iPhone 15 Pro"));

        // 3. Actualizar producto
        ProductoUpdateDTO updateDTO = new ProductoUpdateDTO();
        updateDTO.setNombre("iPhone 15 Pro Actualizado");
        updateDTO.setPrecio(1299);
        updateDTO.setStock(30);
        updateDTO.setCategoria("SMARTPHONES");
        
        mockMvc.perform(put("/api/productos/{id}", idProducto)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("iPhone 15 Pro Actualizado"));

        // 4. Buscar por nombre
        mockMvc.perform(get("/api/productos/buscar")
                .param("nombre", "Actualizado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        // 5. Actualizar stock
        mockMvc.perform(patch("/api/productos/{id}/stock", idProducto)
                .param("cantidad", "-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(20));

        // 6. Eliminar producto
        mockMvc.perform(delete("/api/productos/{id}", idProducto))
                .andExpect(status().isNoContent());

        // Verificar que fue eliminado
        mockMvc.perform(get("/api/productos/{id}", idProducto))
                .andExpect(status().isNotFound());
    }
}
