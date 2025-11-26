package com.appmovil.msvc.pedidos.controllers;

import com.appmovil.msvc.pedidos.clients.PagoClientRest;
import com.appmovil.msvc.pedidos.clients.ProductoClientRest;
import com.appmovil.msvc.pedidos.clients.UsuarioClientRest;
import com.appmovil.msvc.pedidos.dtos.PedidoCreationDTO;
import com.appmovil.msvc.pedidos.dtos.PedidoDetalleDTO;
import com.appmovil.msvc.pedidos.model.Producto;
import com.appmovil.msvc.pedidos.model.Usuario;
import com.appmovil.msvc.pedidos.model.entity.Pedido;
import com.appmovil.msvc.pedidos.model.entity.PedidoDetalle;
import com.appmovil.msvc.pedidos.repositories.PedidoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("PedidoController - Integration Tests")
class PedidoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PedidoRepository pedidoRepository;

    @MockBean
    private UsuarioClientRest usuarioClientRest;

    @MockBean
    private ProductoClientRest productoClientRest;

    @MockBean
    private PagoClientRest pagoClientRest;

    private Usuario usuarioTest;
    private Producto productoTest;
    private PedidoCreationDTO creationDTO;

    @BeforeEach
    void setUp() {
        pedidoRepository.deleteAll();

        // Usuario mock
        usuarioTest = new Usuario();
        usuarioTest.setIdUsuario(1L);
        usuarioTest.setNombre("Juan");
        usuarioTest.setApellido("Pérez");
        usuarioTest.setEmail("juan@email.com");
        usuarioTest.setActivo(true);

        // Producto mock
        productoTest = new Producto();
        productoTest.setIdProducto(1L);
        productoTest.setNombre("Laptop Dell");
        productoTest.setPrecio(1200);
        productoTest.setStock(10);
        productoTest.setActivo(true);

        // DTO de creación
        creationDTO = new PedidoCreationDTO();
        creationDTO.setIdUsuario(1L);
        creationDTO.setDireccionEnvio("Av. Principal 123, Lima");
        creationDTO.setMetodoPago("TARJETA");
        creationDTO.setNotas("Entrega en horario laboral");
        
        PedidoDetalleDTO detalleDTO = new PedidoDetalleDTO();
        detalleDTO.setIdProducto(1L);
        detalleDTO.setCantidad(2);
        creationDTO.setDetalles(Arrays.asList(detalleDTO));
    }

    @Test
    @DisplayName("POST /api/pedidos - Debe crear pedido exitosamente")
    void testCrearPedido_ConDatosValidos_DebeRetornar201() throws Exception {
        // Given
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);
        when(productoClientRest.updateStock(eq(1L), eq(-2))).thenReturn(productoTest);

        // When & Then
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.direccionEnvio").value("Av. Principal 123, Lima"))
                .andExpect(jsonPath("$.metodoPago").value("TARJETA"))
                .andExpect(jsonPath("$.notas").value("Entrega en horario laboral"))
                .andExpect(jsonPath("$.detalles", hasSize(1)))
                .andExpect(jsonPath("$.detalles[0].cantidad").value(2))
                .andExpect(jsonPath("$.fechaPedido").exists())
                .andExpect(jsonPath("$.subtotal").exists())
                .andExpect(jsonPath("$.total").exists());

        verify(usuarioClientRest).findById(1L);
        verify(productoClientRest).findById(1L);
        verify(productoClientRest).updateStock(1L, -2);
    }

    @Test
    @DisplayName("POST /api/pedidos - Debe retornar 400 con campos requeridos faltantes")
    void testCrearPedido_SinCamposRequeridos_DebeRetornar400() throws Exception {
        // Given
        PedidoCreationDTO dtoInvalido = new PedidoCreationDTO();
        // No se establecen campos requeridos

        // When & Then
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/pedidos - Debe retornar 400 cuando usuario no existe")
    void testCrearPedido_ConUsuarioInexistente_DebeRetornar400() throws Exception {
        // Given
        when(usuarioClientRest.findById(1L)).thenThrow(mock(FeignException.class));

        // When & Then
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isBadRequest());

        verify(usuarioClientRest).findById(1L);
    }

    @Test
    @DisplayName("POST /api/pedidos - Debe retornar 400 cuando producto no tiene stock")
    void testCrearPedido_ConStockInsuficiente_DebeRetornar400() throws Exception {
        // Given
        productoTest.setStock(1); // Stock menor que cantidad solicitada
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When & Then
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Stock insuficiente")));

        verify(productoClientRest, never()).updateStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("POST /api/pedidos - Debe retornar 400 cuando producto está inactivo")
    void testCrearPedido_ConProductoInactivo_DebeRetornar400() throws Exception {
        // Given
        productoTest.setActivo(false);
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When & Then
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("no está activo")));
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} - Debe retornar pedido con datos enriquecidos")
    void testFindById_ConIdValido_DebeRetornarPedido() throws Exception {
        // Given
        Pedido pedido = crearPedidoEnDB();
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);

        // When & Then
        mockMvc.perform(get("/api/pedidos/{id}", pedido.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPedido").value(pedido.getId()))
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.nombreUsuario").value("Juan Pérez"))
                .andExpect(jsonPath("$.emailUsuario").value("juan@email.com"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        verify(usuarioClientRest).findById(1L);
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} - Debe retornar 404 con ID inexistente")
    void testFindById_ConIdInexistente_DebeRetornar404() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/pedidos/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("999")));
    }

    @Test
    @DisplayName("GET /api/pedidos/usuario/{idUsuario} - Debe retornar pedidos del usuario")
    void testFindByUsuario_DebeRetornarListaPedidos() throws Exception {
        // Given
        crearPedidoEnDB();
        when(usuarioClientRest.findById(anyLong())).thenReturn(usuarioTest);

        // When & Then
        mockMvc.perform(get("/api/pedidos/usuario/{idUsuario}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].idUsuario").value(1));
    }

    @Test
    @DisplayName("GET /api/pedidos/estado/{estado} - Debe retornar pedidos por estado")
    void testFindByEstado_DebeRetornarPedidosFiltrados() throws Exception {
        // Given
        crearPedidoEnDB();
        when(usuarioClientRest.findById(anyLong())).thenReturn(usuarioTest);

        // When & Then
        mockMvc.perform(get("/api/pedidos/estado/{estado}", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("PUT /api/pedidos/{id}/estado - Debe actualizar estado del pedido")
    void testActualizarEstado_DebeActualizarExitosamente() throws Exception {
        // Given
        Pedido pedido = crearPedidoEnDB();
        when(usuarioClientRest.findById(anyLong())).thenReturn(usuarioTest);

        // When & Then
        mockMvc.perform(put("/api/pedidos/{id}/estado", pedido.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("estado", "ENVIADO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENVIADO"));

        // Verificar en DB
        Pedido actualizado = pedidoRepository.findById(pedido.getId()).orElseThrow();
        assertThat(actualizado.getEstado()).isEqualTo("ENVIADO");
    }

    @Test
    @DisplayName("POST /api/pedidos/{id}/cancelar - Debe cancelar pedido y restaurar stock")
    void testCancelarPedido_DebeCancelarYRestaurarStock() throws Exception {
        // Given
        Pedido pedido = crearPedidoEnDB();
        when(usuarioClientRest.findById(anyLong())).thenReturn(usuarioTest);
        when(productoClientRest.updateStock(anyLong(), anyInt())).thenReturn(productoTest);

        // When & Then
        mockMvc.perform(post("/api/pedidos/{id}/cancelar", pedido.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("cancelado")));

        // Verificar que se restauró el stock
        verify(productoClientRest).updateStock(eq(1L), eq(2)); // Restaura las 2 unidades

        // Verificar estado en DB
        Pedido cancelado = pedidoRepository.findById(pedido.getId()).orElseThrow();
        assertThat(cancelado.getEstado()).isEqualTo("CANCELADO");
    }

    @Test
    @DisplayName("POST /api/pedidos/{id}/cancelar - Debe retornar 400 si pedido no está PENDIENTE")
    void testCancelarPedido_ConEstadoNoPendiente_DebeRetornar400() throws Exception {
        // Given
        Pedido pedido = crearPedidoEnDB();
        pedido.setEstado("ENVIADO");
        pedidoRepository.save(pedido);

        // When & Then
        mockMvc.perform(post("/api/pedidos/{id}/cancelar", pedido.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("PENDIENTE")));

        verify(productoClientRest, never()).updateStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("GET /api/pedidos - Debe retornar todos los pedidos")
    void testFindAll_DebeRetornarListaCompleta() throws Exception {
        // Given
        crearPedidoEnDB();
        when(usuarioClientRest.findById(anyLong())).thenReturn(usuarioTest);

        // When & Then
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Flujo completo - Crear, consultar, actualizar estado y cancelar pedido")
    void testFlujoCompletoPedido() throws Exception {
        // Given
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);
        when(productoClientRest.updateStock(anyLong(), anyInt())).thenReturn(productoTest);

        // 1. Crear pedido
        String response = mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long idPedido = objectMapper.readTree(response).get("idPedido").asLong();

        // 2. Consultar pedido creado
        mockMvc.perform(get("/api/pedidos/{id}", idPedido))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        // 3. Actualizar estado a PAGADO
        mockMvc.perform(put("/api/pedidos/{id}/estado", idPedido)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("estado", "PAGADO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PAGADO"));

        // 4. Cambiar de vuelta a PENDIENTE para poder cancelar
        Pedido pedido = pedidoRepository.findById(idPedido).orElseThrow();
        pedido.setEstado("PENDIENTE");
        pedidoRepository.save(pedido);

        // 5. Cancelar pedido (debe restaurar stock)
        mockMvc.perform(post("/api/pedidos/{id}/cancelar", idPedido))
                .andExpect(status().isOk());

        // Verificar que se llamó updateStock dos veces: -2 al crear y +2 al cancelar
        verify(productoClientRest, times(2)).updateStock(eq(1L), anyInt());
    }

    @Test
    @DisplayName("POST /api/pedidos - Debe crear pedido con múltiples productos")
    void testCrearPedido_ConMultiplesProductos_DebeCrearExitosamente() throws Exception {
        // Given
        Producto producto2 = new Producto();
        producto2.setIdProducto(2L);
        producto2.setNombre("Mouse Logitech");
        producto2.setPrecio(50);
        producto2.setStock(20);
        producto2.setActivo(true);

        PedidoDetalleDTO detalle1 = new PedidoDetalleDTO();
        detalle1.setIdProducto(1L);
        detalle1.setCantidad(1);

        PedidoDetalleDTO detalle2 = new PedidoDetalleDTO();
        detalle2.setIdProducto(2L);
        detalle2.setCantidad(3);

        creationDTO.setDetalles(Arrays.asList(detalle1, detalle2));

        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);
        when(productoClientRest.findById(2L)).thenReturn(producto2);
        when(productoClientRest.updateStock(anyLong(), anyInt())).thenReturn(productoTest);

        // When & Then
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.detalles", hasSize(2)))
                .andExpect(jsonPath("$.detalles[0].cantidad").value(1))
                .andExpect(jsonPath("$.detalles[1].cantidad").value(3));

        verify(productoClientRest).updateStock(1L, -1);
        verify(productoClientRest).updateStock(2L, -3);
    }

    // Método helper para crear un pedido en la DB
    private Pedido crearPedidoEnDB() {
        Pedido pedido = new Pedido();
        pedido.setIdUsuario(1L);
        pedido.setEstado("PENDIENTE");
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setDireccionEnvio("Av. Principal 123, Lima");
        pedido.setMetodoPago("TARJETA");
        pedido.setNotas("Entrega en horario laboral");
        pedido.setSubtotal(2400);
        pedido.setTotal(2400);
        pedido.setDetalles(new ArrayList<>());

        PedidoDetalle detalle = new PedidoDetalle();
        detalle.setPedido(pedido);
        detalle.setIdProducto(1L);
        detalle.setCantidad(2);
        detalle.setPrecioUnitario(1200);

        pedido.getDetalles().add(detalle);

        return pedidoRepository.save(pedido);
    }
}
