package com.appmovil.msvc.carrito.controllers;

import com.appmovil.msvc.carrito.clients.ProductoClientRest;
import com.appmovil.msvc.carrito.clients.UsuarioClientRest;
import com.appmovil.msvc.carrito.models.Producto;
import com.appmovil.msvc.carrito.models.Usuario;
import com.appmovil.msvc.carrito.models.entities.Carrito;
import com.appmovil.msvc.carrito.models.entities.ItemCarrito;
import com.appmovil.msvc.carrito.repositories.CarritoRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CarritoController - Integration Tests")
class CarritoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CarritoRepository carritoRepository;

    @MockBean
    private UsuarioClientRest usuarioClientRest;

    @MockBean
    private ProductoClientRest productoClientRest;

    private Usuario usuarioTest;
    private Producto productoTest;

    @BeforeEach
    void setUp() {
        carritoRepository.deleteAll();

        // Usuario mock
        usuarioTest = new Usuario();
        usuarioTest.setIdUsuario(1L);
        usuarioTest.setNombre("Carlos");
        usuarioTest.setApellido("Ramírez");
        usuarioTest.setEmail("carlos@email.com");
        usuarioTest.setActivo(true);

        // Producto mock
        productoTest = new Producto();
        productoTest.setIdProducto(1L);
        productoTest.setNombre("Samsung Galaxy S23");
        productoTest.setPrecio(BigDecimal.valueOf(899));
        productoTest.setStock(15);
        productoTest.setActivo(true);
    }

    @Test
    @DisplayName("GET /api/carrito/{idUsuario} - Debe crear carrito si no existe")
    void testObtenerCarrito_NoExiste_DebeCrear() throws Exception {
        // Given
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);

        // When & Then
        mockMvc.perform(get("/api/carrito/{idUsuario}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.totalItems").value(0));

        // Verificar que se creó en DB
        assertThat(carritoRepository.findByIdUsuarioAndEstado(1L, "ACTIVO")).isPresent();
    }

    @Test
    @DisplayName("GET /api/carrito/{idUsuario} - Debe retornar carrito existente con items")
    void testObtenerCarrito_Existente_DebeRetornarConItems() throws Exception {
        // Given
        Carrito carrito = crearCarritoConItems();
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When & Then
        mockMvc.perform(get("/api/carrito/{idUsuario}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCarrito").value(carrito.getIdCarrito()))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].cantidad").value(3))
                .andExpect(jsonPath("$.items[0].nombreProducto").value("Samsung Galaxy S23"))
                .andExpect(jsonPath("$.total").value(2697)) // 899 * 3
                .andExpect(jsonPath("$.totalItems").value(3));
    }

    @Test
    @DisplayName("POST /api/carrito/{idUsuario}/items - Debe agregar item nuevo")
    void testAgregarItem_Nuevo_DebeAgregarExitosamente() throws Exception {
        // Given
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        Map<String, Object> request = Map.of(
                "idProducto", 1L,
                "cantidad", 2
        );

        // When & Then
        mockMvc.perform(post("/api/carrito/{idUsuario}/items", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].idProducto").value(1))
                .andExpect(jsonPath("$.items[0].cantidad").value(2))
                .andExpect(jsonPath("$.items[0].subtotal").value(1798)) // 899 * 2
                .andExpect(jsonPath("$.total").value(1798));
    }

    @Test
    @DisplayName("POST /api/carrito/{idUsuario}/items - Debe incrementar cantidad si item existe")
    void testAgregarItem_Existente_DebeIncrementarCantidad() throws Exception {
        // Given
        crearCarritoConItems(); // Ya tiene 3 unidades
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        Map<String, Object> request = Map.of(
                "idProducto", 1L,
                "cantidad", 2
        );

        // When & Then
        mockMvc.perform(post("/api/carrito/{idUsuario}/items", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].cantidad").value(5)); // 3 + 2
    }

    @Test
    @DisplayName("POST /api/carrito/{idUsuario}/items - Debe retornar 400 con más de 10 unidades")
    void testAgregarItem_MasDe10Unidades_DebeRetornar400() throws Exception {
        // Given
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        Map<String, Object> request = Map.of(
                "idProducto", 1L,
                "cantidad", 11
        );

        // When & Then
        mockMvc.perform(post("/api/carrito/{idUsuario}/items", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("10 unidades")));
    }

    @Test
    @DisplayName("POST /api/carrito/{idUsuario}/items - Debe validar stock insuficiente")
    void testAgregarItem_StockInsuficiente_DebeRetornar400() throws Exception {
        // Given
        productoTest.setStock(1); // Stock menor que cantidad solicitada
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        Map<String, Object> request = Map.of(
                "idProducto", 1L,
                "cantidad", 5
        );

        // When & Then
        mockMvc.perform(post("/api/carrito/{idUsuario}/items", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Stock insuficiente")));
    }

    @Test
    @DisplayName("POST /api/carrito/{idUsuario}/items - Debe validar producto activo")
    void testAgregarItem_ProductoInactivo_DebeRetornar400() throws Exception {
        // Given
        productoTest.setActivo(false);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        Map<String, Object> request = Map.of(
                "idProducto", 1L,
                "cantidad", 2
        );

        // When & Then
        mockMvc.perform(post("/api/carrito/{idUsuario}/items", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("no está disponible")));
    }

    @Test
    @DisplayName("PUT /api/carrito/{idUsuario}/items/{idProducto} - Debe actualizar cantidad")
    void testActualizarCantidad_DebeActualizarExitosamente() throws Exception {
        // Given
        crearCarritoConItems();
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        Map<String, Object> request = Map.of("cantidad", 7);

        // When & Then
        mockMvc.perform(put("/api/carrito/{idUsuario}/items/{idProducto}", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].cantidad").value(7))
                .andExpect(jsonPath("$.items[0].subtotal").value(6293)); // 899 * 7
    }

    @Test
    @DisplayName("PUT /api/carrito/{idUsuario}/items/{idProducto} - Debe retornar 404 si item no existe")
    void testActualizarCantidad_ItemNoExiste_DebeRetornar404() throws Exception {
        // Given
        crearCarritoVacio();

        Map<String, Object> request = Map.of("cantidad", 5);

        // When & Then
        mockMvc.perform(put("/api/carrito/{idUsuario}/items/{idProducto}", 1L, 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Item")));
    }

    @Test
    @DisplayName("DELETE /api/carrito/{idUsuario}/items/{idProducto} - Debe remover item")
    void testRemoverItem_DebeRemoverExitosamente() throws Exception {
        // Given
        crearCarritoConItems();
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);

        // When & Then
        mockMvc.perform(delete("/api/carrito/{idUsuario}/items/{idProducto}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @DisplayName("DELETE /api/carrito/{idUsuario}/items/{idProducto} - Debe retornar 404 si item no existe")
    void testRemoverItem_ItemNoExiste_DebeRetornar404() throws Exception {
        // Given
        crearCarritoVacio();

        // When & Then
        mockMvc.perform(delete("/api/carrito/{idUsuario}/items/{idProducto}", 1L, 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/carrito/{idUsuario} - Debe vaciar carrito")
    void testVaciarCarrito_DebeLimpiarItems() throws Exception {
        // Given
        crearCarritoConItems();
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);

        // When & Then
        mockMvc.perform(delete("/api/carrito/{idUsuario}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.total").value(0));

        // Verificar en DB
        Carrito carrito = carritoRepository.findByIdUsuarioAndEstado(1L, "ACTIVO").orElseThrow();
        assertThat(carrito.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Flujo completo - Crear carrito, agregar items, actualizar, remover y vaciar")
    void testFlujoCompletoCarrito() throws Exception {
        // Given
        when(usuarioClientRest.findById(anyLong())).thenReturn(usuarioTest);
        when(productoClientRest.findById(anyLong())).thenReturn(productoTest);

        Producto producto2 = new Producto();
        producto2.setIdProducto(2L);
        producto2.setNombre("Producto 2");
        producto2.setPrecio(BigDecimal.valueOf(500));
        producto2.setStock(20);
        producto2.setActivo(true);
        when(productoClientRest.findById(2L)).thenReturn(producto2);

        // 1. Obtener carrito (se crea automáticamente)
        mockMvc.perform(get("/api/carrito/{idUsuario}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));

        // 2. Agregar primer producto
        mockMvc.perform(post("/api/carrito/{idUsuario}/items", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("idProducto", 1L, "cantidad", 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.totalItems").value(3));

        // 3. Agregar segundo producto
        mockMvc.perform(post("/api/carrito/{idUsuario}/items", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("idProducto", 2L, "cantidad", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.totalItems").value(5));

        // 4. Actualizar cantidad del primer producto
        mockMvc.perform(put("/api/carrito/{idUsuario}/items/{idProducto}", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("cantidad", 5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].cantidad").value(5));

        // 5. Remover segundo producto
        mockMvc.perform(delete("/api/carrito/{idUsuario}/items/{idProducto}", 1L, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)));

        // 6. Vaciar carrito
        mockMvc.perform(delete("/api/carrito/{idUsuario}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @DisplayName("POST /api/carrito/{idUsuario}/items - Debe calcular subtotales correctamente")
    void testAgregarItem_DebeCalcularSubtotales() throws Exception {
        // Given
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        Map<String, Object> request = Map.of("idProducto", 1L, "cantidad", 4);

        // When & Then
        mockMvc.perform(post("/api/carrito/{idUsuario}/items", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].precioUnitario").value(899))
                .andExpect(jsonPath("$.items[0].subtotal").value(3596)) // 899 * 4
                .andExpect(jsonPath("$.total").value(3596));
    }

    @Test
    @DisplayName("GET /api/carrito/{idUsuario} - Debe incluir datos de usuario enriquecidos")
    void testObtenerCarrito_DebeIncluirDatosUsuario() throws Exception {
        // Given
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);

        // When & Then
        mockMvc.perform(get("/api/carrito/{idUsuario}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("Carlos Ramírez"))
                .andExpect(jsonPath("$.emailUsuario").value("carlos@email.com"));
    }

    @Test
    @DisplayName("POST /api/carrito/{idUsuario}/items - Debe validar límite acumulado de 10 unidades")
    void testAgregarItem_LimiteAcumulado_DebeRetornar400() throws Exception {
        // Given
        crearCarritoConItems(); // Ya tiene 3 unidades
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        Map<String, Object> request = Map.of("idProducto", 1L, "cantidad", 8); // 3 + 8 = 11 > 10

        // When & Then
        mockMvc.perform(post("/api/carrito/{idUsuario}/items", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("10 unidades")));
    }

    // Método helper para crear carrito vacío
    private Carrito crearCarritoVacio() {
        Carrito carrito = new Carrito();
        carrito.setIdUsuario(1L);
        carrito.setEstado("ACTIVO");
        carrito.setFechaCreacion(LocalDateTime.now());
        carrito.setItems(new ArrayList<>());
        carrito.setTotal(BigDecimal.ZERO);
        return carritoRepository.save(carrito);
    }

    // Método helper para crear carrito con items
    private Carrito crearCarritoConItems() {
        Carrito carrito = new Carrito();
        carrito.setIdUsuario(1L);
        carrito.setEstado("ACTIVO");
        carrito.setFechaCreacion(LocalDateTime.now());
        carrito.setItems(new ArrayList<>());

        ItemCarrito item = new ItemCarrito();
        item.setCarrito(carrito);
        item.setIdProducto(1L);
        item.setCantidad(3);
        item.setPrecioUnitario(BigDecimal.valueOf(899));
        item.setSubtotal(BigDecimal.valueOf(2697));

        carrito.getItems().add(item);
        carrito.setTotal(BigDecimal.valueOf(2697));

        return carritoRepository.save(carrito);
    }
}
