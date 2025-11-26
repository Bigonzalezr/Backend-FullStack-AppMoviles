package com.appmovil.msvc.pagos.controllers;

import com.appmovil.msvc.pagos.clients.PedidoClientRest;
import com.appmovil.msvc.pagos.clients.UsuarioClientRest;
import com.appmovil.msvc.pagos.dtos.ProcesarPagoDTO;
import com.appmovil.msvc.pagos.models.Pedido;
import com.appmovil.msvc.pagos.models.Usuario;
import com.appmovil.msvc.pagos.models.entities.Pago;
import com.appmovil.msvc.pagos.repositories.PagoRepository;
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
@DisplayName("PagoController - Integration Tests")
class PagoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PagoRepository pagoRepository;

    @MockBean
    private UsuarioClientRest usuarioClientRest;

    @MockBean
    private PedidoClientRest pedidoClientRest;

    private Usuario usuarioTest;
    private Pedido pedidoTest;
    private ProcesarPagoDTO procesarPagoDTO;

    @BeforeEach
    void setUp() {
        pagoRepository.deleteAll();

        usuarioTest = new Usuario();
        usuarioTest.setIdUsuario(1L);
        usuarioTest.setNombre("Pedro");
        usuarioTest.setApellido("López");
        usuarioTest.setEmail("pedro@email.com");
        usuarioTest.setActivo(true);

        pedidoTest = new Pedido();
        pedidoTest.setIdPedido(1L);
        pedidoTest.setIdUsuario(1L);
        pedidoTest.setTotal(new BigDecimal("2500"));
        pedidoTest.setEstado("PENDIENTE");

        procesarPagoDTO = new ProcesarPagoDTO();
        procesarPagoDTO.setIdPedido(1L);
        procesarPagoDTO.setIdUsuario(1L);
        procesarPagoDTO.setMonto(new BigDecimal("2500.00"));
        procesarPagoDTO.setMetodoPago("TARJETA_CREDITO");
        procesarPagoDTO.setGatewayPago("Stripe");
        procesarPagoDTO.setDescripcion("Pago de pedido #1");

        when(usuarioClientRest.findById(anyLong())).thenReturn(usuarioTest);
        when(pedidoClientRest.findById(anyLong())).thenReturn(pedidoTest);
        doNothing().when(pedidoClientRest).actualizarEstadoPago(anyLong());
    }

    @Test
    @DisplayName("POST /api/pagos/procesar - Debe procesar pago exitosamente")
    void testProcesarPago_ConDatosValidos_DebeRetornar200() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/pagos/procesar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(procesarPagoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPago").exists())
                .andExpect(jsonPath("$.idPedido").value(1))
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.monto").value(2500.00))
                .andExpect(jsonPath("$.metodoPago").value("TARJETA_CREDITO"))
                .andExpect(jsonPath("$.estado").value("COMPLETADO"))
                .andExpect(jsonPath("$.numeroTransaccion").exists())
                .andExpect(jsonPath("$.numeroAutorizacion").exists())
                .andExpect(jsonPath("$.nombreUsuario").value("Pedro López"));

        assertThat(pagoRepository.count()).isEqualTo(1);
        verify(pedidoClientRest).actualizarEstadoPago(1L);
    }

    @Test
    @DisplayName("POST /api/pagos/procesar - Debe retornar 400 con campos inválidos")
    void testProcesarPago_ConCamposInvalidos_DebeRetornar400() throws Exception {
        // Given
        procesarPagoDTO.setMonto(new BigDecimal("0")); // Monto inválido

        // When & Then
        mockMvc.perform(post("/api/pagos/procesar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(procesarPagoDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/pagos/procesar - Debe retornar 400 con pago duplicado")
    void testProcesarPago_ConPagoDuplicado_DebeRetornar400() throws Exception {
        // Given - Crear primer pago
        mockMvc.perform(post("/api/pagos/procesar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(procesarPagoDTO)))
                .andExpect(status().isOk());

        // When & Then - Intentar segundo pago para mismo pedido
        mockMvc.perform(post("/api/pagos/procesar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(procesarPagoDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Ya existe un pago")));
    }

    @Test
    @DisplayName("GET /api/pagos/{id} - Debe retornar pago por ID")
    void testFindById_ConIdValido_DebeRetornarPago() throws Exception {
        // Given
        Pago pago = Pago.builder()
                .idPedido(1L)
                .idUsuario(1L)
                .monto(new BigDecimal("2500.00"))
                .metodoPago("TARJETA_CREDITO")
                .estado("COMPLETADO")
                .fechaPago(LocalDateTime.now())
                .numeroTransaccion("TXN-123456")
                .build();
        Pago guardado = pagoRepository.save(pago);

        // When & Then
        mockMvc.perform(get("/api/pagos/{id}", guardado.getIdPago()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPago").value(guardado.getIdPago()))
                .andExpect(jsonPath("$.estado").value("COMPLETADO"))
                .andExpect(jsonPath("$.nombreUsuario").value("Pedro López"));
    }

    @Test
    @DisplayName("GET /api/pagos/{id} - Debe retornar 404 con ID inexistente")
    void testFindById_ConIdInexistente_DebeRetornar404() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/pagos/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/pagos - Debe retornar lista de pagos")
    void testFindAll_DebeRetornarLista() throws Exception {
        // Given
        Pago pago = Pago.builder()
                .idPedido(1L)
                .idUsuario(1L)
                .monto(new BigDecimal("2500.00"))
                .metodoPago("TARJETA_CREDITO")
                .estado("COMPLETADO")
                .fechaPago(LocalDateTime.now())
                .build();
        pagoRepository.save(pago);

        // When & Then
        mockMvc.perform(get("/api/pagos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].estado").value("COMPLETADO"));
    }

    @Test
    @DisplayName("GET /api/pagos/usuario/{idUsuario} - Debe retornar pagos del usuario")
    void testFindByUsuario_DebeRetornarPagosDelUsuario() throws Exception {
        // Given
        Pago pago = Pago.builder()
                .idPedido(1L)
                .idUsuario(1L)
                .monto(new BigDecimal("2500.00"))
                .metodoPago("TARJETA_CREDITO")
                .estado("COMPLETADO")
                .fechaPago(LocalDateTime.now())
                .build();
        pagoRepository.save(pago);

        // When & Then
        mockMvc.perform(get("/api/pagos/usuario/{idUsuario}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idUsuario").value(1));
    }

    @Test
    @DisplayName("GET /api/pagos/pedido/{idPedido} - Debe retornar pago del pedido")
    void testFindByPedido_DebeRetornarPago() throws Exception {
        // Given
        Pago pago = Pago.builder()
                .idPedido(1L)
                .idUsuario(1L)
                .monto(new BigDecimal("2500.00"))
                .metodoPago("TARJETA_CREDITO")
                .estado("COMPLETADO")
                .fechaPago(LocalDateTime.now())
                .build();
        pagoRepository.save(pago);

        // When & Then
        mockMvc.perform(get("/api/pagos/pedido/{idPedido}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPedido").value(1));
    }

    @Test
    @DisplayName("GET /api/pagos/estado/{estado} - Debe filtrar por estado")
    void testFindByEstado_DebeFiltrarPorEstado() throws Exception {
        // Given
        Pago completado = Pago.builder()
                .idPedido(1L)
                .idUsuario(1L)
                .monto(new BigDecimal("2500.00"))
                .metodoPago("TARJETA_CREDITO")
                .estado("COMPLETADO")
                .fechaPago(LocalDateTime.now())
                .build();
        pagoRepository.save(completado);

        Pago pendiente = Pago.builder()
                .idPedido(2L)
                .idUsuario(1L)
                .monto(new BigDecimal("1000.00"))
                .metodoPago("PAYPAL")
                .estado("PENDIENTE")
                .fechaPago(LocalDateTime.now())
                .build();
        pagoRepository.save(pendiente);

        // When & Then
        mockMvc.perform(get("/api/pagos/estado/{estado}", "COMPLETADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].estado").value("COMPLETADO"));
    }

    @Test
    @DisplayName("GET /api/pagos/metodo/{metodoPago} - Debe filtrar por método de pago")
    void testFindByMetodoPago_DebeFiltrarPorMetodo() throws Exception {
        // Given
        Pago pago = Pago.builder()
                .idPedido(1L)
                .idUsuario(1L)
                .monto(new BigDecimal("2500.00"))
                .metodoPago("TARJETA_CREDITO")
                .estado("COMPLETADO")
                .fechaPago(LocalDateTime.now())
                .build();
        pagoRepository.save(pago);

        // When & Then
        mockMvc.perform(get("/api/pagos/metodo/{metodoPago}", "TARJETA_CREDITO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].metodoPago").value("TARJETA_CREDITO"));
    }

    @Test
    @DisplayName("POST /api/pagos/{id}/reembolsar - Debe reembolsar pago")
    void testReembolsarPago_ConPagoCompletado_DebeReembolsar() throws Exception {
        // Given
        Pago pago = Pago.builder()
                .idPedido(1L)
                .idUsuario(1L)
                .monto(new BigDecimal("2500.00"))
                .metodoPago("TARJETA_CREDITO")
                .estado("COMPLETADO")
                .fechaPago(LocalDateTime.now())
                .descripcion("Pago original")
                .build();
        Pago guardado = pagoRepository.save(pago);

        // When & Then
        mockMvc.perform(post("/api/pagos/{id}/reembolsar", guardado.getIdPago())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("motivo", "Producto defectuoso"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REEMBOLSADO"))
                .andExpect(jsonPath("$.descripcion", containsString("Reembolso")));

        Pago actualizado = pagoRepository.findById(guardado.getIdPago()).orElseThrow();
        assertThat(actualizado.getEstado()).isEqualTo("REEMBOLSADO");
    }

    @Test
    @DisplayName("POST /api/pagos/{id}/reembolsar - Debe retornar 400 si pago no completado")
    void testReembolsarPago_ConPagoPendiente_DebeRetornar400() throws Exception {
        // Given
        Pago pago = Pago.builder()
                .idPedido(1L)
                .idUsuario(1L)
                .monto(new BigDecimal("2500.00"))
                .metodoPago("TARJETA_CREDITO")
                .estado("PENDIENTE")
                .fechaPago(LocalDateTime.now())
                .build();
        Pago guardado = pagoRepository.save(pago);

        // When & Then
        mockMvc.perform(post("/api/pagos/{id}/reembolsar", guardado.getIdPago())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("motivo", "Motivo"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("completados")));
    }

    @Test
    @DisplayName("GET /api/pagos/{id}/verificar - Debe verificar pago")
    void testVerificarPago_ConIdValido_DebeVerificar() throws Exception {
        // Given
        Pago pago = Pago.builder()
                .idPedido(1L)
                .idUsuario(1L)
                .monto(new BigDecimal("2500.00"))
                .metodoPago("TARJETA_CREDITO")
                .estado("COMPLETADO")
                .fechaPago(LocalDateTime.now())
                .numeroTransaccion("TXN-123456")
                .build();
        Pago guardado = pagoRepository.save(pago);

        // When & Then
        mockMvc.perform(get("/api/pagos/{id}/verificar", guardado.getIdPago()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO"));
    }

    @Test
    @DisplayName("Flujo completo - Procesar, consultar, verificar y reembolsar pago")
    void testFlujoCompletoPago() throws Exception {
        // 1. Procesar pago
        String response = mockMvc.perform(post("/api/pagos/procesar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(procesarPagoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO"))
                .andReturn().getResponse().getContentAsString();

        Long idPago = objectMapper.readTree(response).get("idPago").asLong();

        // 2. Consultar pago creado
        mockMvc.perform(get("/api/pagos/{id}", idPago))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroTransaccion").exists());

        // 3. Verificar pago
        mockMvc.perform(get("/api/pagos/{id}/verificar", idPago))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO"));

        // 4. Consultar pagos del usuario
        mockMvc.perform(get("/api/pagos/usuario/{idUsuario}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // 5. Reembolsar pago
        mockMvc.perform(post("/api/pagos/{id}/reembolsar", idPago)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("motivo", "Cliente insatisfecho"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REEMBOLSADO"));

        // Verificar estado final
        Pago pagoFinal = pagoRepository.findById(idPago).orElseThrow();
        assertThat(pagoFinal.getEstado()).isEqualTo("REEMBOLSADO");
    }
}
