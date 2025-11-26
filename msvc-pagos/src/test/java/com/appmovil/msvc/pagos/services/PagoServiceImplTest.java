package com.appmovil.msvc.pagos.services;

import com.appmovil.msvc.pagos.clients.PedidoClientRest;
import com.appmovil.msvc.pagos.clients.UsuarioClientRest;
import com.appmovil.msvc.pagos.dtos.PagoDTO;
import com.appmovil.msvc.pagos.dtos.ProcesarPagoDTO;
import com.appmovil.msvc.pagos.exceptions.PagoException;
import com.appmovil.msvc.pagos.exceptions.ResourceNotFoundException;
import com.appmovil.msvc.pagos.models.Pedido;
import com.appmovil.msvc.pagos.models.Usuario;
import com.appmovil.msvc.pagos.models.entities.Pago;
import com.appmovil.msvc.pagos.repositories.PagoRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PagoService - Unit Tests")
class PagoServiceImplTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private UsuarioClientRest usuarioClientRest;

    @Mock
    private PedidoClientRest pedidoClientRest;

    @InjectMocks
    private PagoServiceImpl pagoService;

    private Pago pagoTest;
    private ProcesarPagoDTO procesarPagoDTO;
    private Usuario usuarioTest;
    private Pedido pedidoTest;

    @BeforeEach
    void setUp() {
        usuarioTest = new Usuario();
        usuarioTest.setIdUsuario(1L);
        usuarioTest.setNombre("María");
        usuarioTest.setApellido("García");
        usuarioTest.setEmail("maria@email.com");
        usuarioTest.setActivo(true);

        pedidoTest = new Pedido();
        pedidoTest.setIdPedido(1L);
        pedidoTest.setIdUsuario(1L);
        pedidoTest.setTotal(new BigDecimal("1500"));
        pedidoTest.setEstado("PENDIENTE");

        pagoTest = Pago.builder()
                .idPago(1L)
                .idPedido(1L)
                .idUsuario(1L)
                .monto(new BigDecimal("1500.00"))
                .metodoPago("TARJETA_CREDITO")
                .estado("COMPLETADO")
                .fechaPago(LocalDateTime.now())
                .fechaProcesamiento(LocalDateTime.now())
                .numeroTransaccion("TXN-12345678")
                .numeroAutorizacion("AUTH-ABC123")
                .gatewayPago("Stripe")
                .descripcion("Pago de pedido #1")
                .build();

        procesarPagoDTO = new ProcesarPagoDTO();
        procesarPagoDTO.setIdPedido(1L);
        procesarPagoDTO.setIdUsuario(1L);
        procesarPagoDTO.setMonto(new BigDecimal("1500.00"));
        procesarPagoDTO.setMetodoPago("TARJETA_CREDITO");
        procesarPagoDTO.setGatewayPago("Stripe");
        procesarPagoDTO.setDescripcion("Pago de pedido #1");
    }

    @Test
    @DisplayName("procesarPago - Debe procesar pago exitosamente")
    void testProcesarPago_ConDatosValidos_DebeProcesarExitosamente() {
        // Given
        when(pagoRepository.findByIdPedido(1L)).thenReturn(Optional.empty());
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(pedidoClientRest.findById(1L)).thenReturn(pedidoTest);
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> {
            Pago p = inv.getArgument(0);
            p.setIdPago(1L);
            return p;
        });
        doNothing().when(pedidoClientRest).actualizarEstadoPago(1L);

        // When
        PagoDTO resultado = pagoService.procesarPago(procesarPagoDTO);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getMonto()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(resultado.getEstado()).isEqualTo("COMPLETADO");
        assertThat(resultado.getNumeroTransaccion()).isNotNull();
        assertThat(resultado.getNumeroAutorizacion()).isNotNull();
        
        verify(usuarioClientRest).findById(1L);
        verify(pedidoClientRest).findById(1L);
        verify(pagoRepository).save(any(Pago.class));
        verify(pedidoClientRest).actualizarEstadoPago(1L);
    }

    @Test
    @DisplayName("procesarPago - Debe lanzar excepción si ya existe pago para el pedido")
    void testProcesarPago_ConPagoDuplicado_DebeLanzarExcepcion() {
        // Given
        when(pagoRepository.findByIdPedido(1L)).thenReturn(Optional.of(pagoTest));

        // When & Then
        assertThatThrownBy(() -> pagoService.procesarPago(procesarPagoDTO))
                .isInstanceOf(PagoException.class)
                .hasMessageContaining("Ya existe un pago");
        
        verify(pagoRepository, never()).save(any());
    }

    @Test
    @DisplayName("procesarPago - Debe lanzar excepción si usuario no existe")
    void testProcesarPago_ConUsuarioInexistente_DebeLanzarExcepcion() {
        // Given
        when(pagoRepository.findByIdPedido(1L)).thenReturn(Optional.empty());
        when(usuarioClientRest.findById(1L)).thenThrow(mock(FeignException.class));

        // When & Then
        assertThatThrownBy(() -> pagoService.procesarPago(procesarPagoDTO))
                .isInstanceOf(PagoException.class)
                .hasMessageContaining("Error al validar el usuario");
        
        verify(pagoRepository, never()).save(any());
    }

    @Test
    @DisplayName("procesarPago - Debe lanzar excepción si usuario está inactivo")
    void testProcesarPago_ConUsuarioInactivo_DebeLanzarExcepcion() {
        // Given
        usuarioTest.setActivo(false);
        when(pagoRepository.findByIdPedido(1L)).thenReturn(Optional.empty());
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);

        // When & Then
        assertThatThrownBy(() -> pagoService.procesarPago(procesarPagoDTO))
                .isInstanceOf(PagoException.class)
                .hasMessageContaining("inactivo");
        
        verify(pagoRepository, never()).save(any());
    }

    @Test
    @DisplayName("procesarPago - Debe lanzar excepción si pedido no existe")
    void testProcesarPago_ConPedidoInexistente_DebeLanzarExcepcion() {
        // Given
        when(pagoRepository.findByIdPedido(1L)).thenReturn(Optional.empty());
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(pedidoClientRest.findById(1L)).thenThrow(mock(FeignException.class));

        // When & Then
        assertThatThrownBy(() -> pagoService.procesarPago(procesarPagoDTO))
                .isInstanceOf(PagoException.class)
                .hasMessageContaining("Error al validar el pedido");
        
        verify(pagoRepository, never()).save(any());
    }

    @Test
    @DisplayName("procesarPago - Debe generar número de transacción único")
    void testProcesarPago_DebeGenerarNumeroTransaccion() {
        // Given
        when(pagoRepository.findByIdPedido(1L)).thenReturn(Optional.empty());
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(pedidoClientRest.findById(1L)).thenReturn(pedidoTest);
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(pedidoClientRest).actualizarEstadoPago(anyLong());

        // When
        PagoDTO resultado = pagoService.procesarPago(procesarPagoDTO);

        // Then
        assertThat(resultado.getNumeroTransaccion()).startsWith("TXN-");
        assertThat(resultado.getNumeroAutorizacion()).isNotNull();
    }

    @Test
    @DisplayName("findById - Debe retornar pago cuando existe")
    void testFindById_ConIdExistente_DebeRetornarPago() {
        // Given
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(pedidoClientRest.findById(1L)).thenReturn(pedidoTest);

        // When
        PagoDTO resultado = pagoService.findById(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdPago()).isEqualTo(1L);
        assertThat(resultado.getNombreUsuario()).isEqualTo("María García");
        verify(pagoRepository).findById(1L);
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando pago no existe")
    void testFindById_ConIdInexistente_DebeLanzarExcepcion() {
        // Given
        when(pagoRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> pagoService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("findAll - Debe retornar lista de pagos enriquecidos")
    void testFindAll_DebeRetornarLista() {
        // Given
        when(pagoRepository.findAll()).thenReturn(Arrays.asList(pagoTest));
        when(usuarioClientRest.findById(anyLong())).thenReturn(usuarioTest);
        when(pedidoClientRest.findById(anyLong())).thenReturn(pedidoTest);

        // When
        List<PagoDTO> resultado = pagoService.findAll();

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getEstado()).isEqualTo("COMPLETADO");
        assertThat(resultado.get(0).getTotalPedido()).isEqualTo(1500);
        verify(pagoRepository).findAll();
    }

    @Test
    @DisplayName("findByUsuario - Debe retornar pagos del usuario")
    void testFindByUsuario_DebeRetornarPagosDelUsuario() {
        // Given
        when(pagoRepository.findByIdUsuario(1L)).thenReturn(Arrays.asList(pagoTest));
        when(usuarioClientRest.findById(anyLong())).thenReturn(usuarioTest);
        when(pedidoClientRest.findById(anyLong())).thenReturn(pedidoTest);

        // When
        List<PagoDTO> resultado = pagoService.findByUsuario(1L);

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdUsuario()).isEqualTo(1L);
        verify(pagoRepository).findByIdUsuario(1L);
    }

    @Test
    @DisplayName("findByPedido - Debe retornar pago del pedido")
    void testFindByPedido_DebeRetornarPago() {
        // Given
        when(pagoRepository.findByIdPedido(1L)).thenReturn(Optional.of(pagoTest));
        when(usuarioClientRest.findById(anyLong())).thenReturn(usuarioTest);
        when(pedidoClientRest.findById(anyLong())).thenReturn(pedidoTest);

        // When
        PagoDTO resultado = pagoService.findByPedido(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdPedido()).isEqualTo(1L);
        verify(pagoRepository).findByIdPedido(1L);
    }

    @Test
    @DisplayName("findByPedido - Debe lanzar excepción si no existe pago para el pedido")
    void testFindByPedido_SinPago_DebeLanzarExcepcion() {
        // Given
        when(pagoRepository.findByIdPedido(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> pagoService.findByPedido(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("findByEstado - Debe filtrar pagos por estado")
    void testFindByEstado_DebeFiltrarPorEstado() {
        // Given
        when(pagoRepository.findByEstado("COMPLETADO")).thenReturn(Arrays.asList(pagoTest));
        when(usuarioClientRest.findById(anyLong())).thenReturn(usuarioTest);
        when(pedidoClientRest.findById(anyLong())).thenReturn(pedidoTest);

        // When
        List<PagoDTO> resultado = pagoService.findByEstado("COMPLETADO");

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getEstado()).isEqualTo("COMPLETADO");
        verify(pagoRepository).findByEstado("COMPLETADO");
    }

    @Test
    @DisplayName("findByMetodoPago - Debe filtrar por método de pago")
    void testFindByMetodoPago_DebeFiltrarPorMetodo() {
        // Given
        when(pagoRepository.findByMetodoPago("TARJETA_CREDITO")).thenReturn(Arrays.asList(pagoTest));
        when(usuarioClientRest.findById(anyLong())).thenReturn(usuarioTest);
        when(pedidoClientRest.findById(anyLong())).thenReturn(pedidoTest);

        // When
        List<PagoDTO> resultado = pagoService.findByMetodoPago("TARJETA_CREDITO");

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getMetodoPago()).isEqualTo("TARJETA_CREDITO");
        verify(pagoRepository).findByMetodoPago("TARJETA_CREDITO");
    }

    @Test
    @DisplayName("findByFechaRango - Debe filtrar por rango de fechas")
    void testFindByFechaRango_DebeFiltrarPorRango() {
        // Given
        LocalDateTime inicio = LocalDateTime.now().minusDays(7);
        LocalDateTime fin = LocalDateTime.now();
        
        when(pagoRepository.findByFechaPagoBetween(inicio, fin)).thenReturn(Arrays.asList(pagoTest));
        when(usuarioClientRest.findById(anyLong())).thenReturn(usuarioTest);
        when(pedidoClientRest.findById(anyLong())).thenReturn(pedidoTest);

        // When
        List<PagoDTO> resultado = pagoService.findByFechaRango(inicio, fin);

        // Then
        assertThat(resultado).hasSize(1);
        verify(pagoRepository).findByFechaPagoBetween(inicio, fin);
    }

    @Test
    @DisplayName("reembolsarPago - Debe reembolsar pago completado")
    void testReembolsarPago_ConPagoCompletado_DebeReembolsar() {
        // Given
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoTest));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioClientRest.findById(anyLong())).thenReturn(usuarioTest);
        when(pedidoClientRest.findById(anyLong())).thenReturn(pedidoTest);

        // When
        PagoDTO resultado = pagoService.reembolsarPago(1L, "Producto defectuoso");

        // Then
        assertThat(resultado.getEstado()).isEqualTo("REEMBOLSADO");
        assertThat(resultado.getDescripcion()).contains("Reembolso");
        verify(pagoRepository).save(argThat(p -> p.getEstado().equals("REEMBOLSADO")));
    }

    @Test
    @DisplayName("reembolsarPago - Debe lanzar excepción si pago no está completado")
    void testReembolsarPago_ConPagoPendiente_DebeLanzarExcepcion() {
        // Given
        pagoTest.setEstado("PENDIENTE");
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoTest));

        // When & Then
        assertThatThrownBy(() -> pagoService.reembolsarPago(1L, "Motivo"))
                .isInstanceOf(PagoException.class)
                .hasMessageContaining("Solo se pueden reembolsar pagos completados");
        
        verify(pagoRepository, never()).save(any());
    }

    @Test
    @DisplayName("verificarPago - Debe verificar pago existente")
    void testVerificarPago_ConIdValido_DebeVerificar() {
        // Given
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoTest));
        when(usuarioClientRest.findById(anyLong())).thenReturn(usuarioTest);
        when(pedidoClientRest.findById(anyLong())).thenReturn(pedidoTest);

        // When
        PagoDTO resultado = pagoService.verificarPago(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getEstado()).isEqualTo("COMPLETADO");
        verify(pagoRepository).findById(1L);
    }

    @Test
    @DisplayName("convertirADTO - Debe manejar errores de Feign gracefully")
    void testConvertirADTO_ConErroresFeign_DebeRetornarDTOSinDatosExtra() {
        // Given
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoTest));
        when(usuarioClientRest.findById(anyLong())).thenThrow(mock(FeignException.class));
        when(pedidoClientRest.findById(anyLong())).thenThrow(mock(FeignException.class));

        // When
        PagoDTO resultado = pagoService.findById(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdPago()).isEqualTo(1L);
        assertThat(resultado.getNombreUsuario()).isNull();
        assertThat(resultado.getTotalPedido()).isNull();
    }
}
