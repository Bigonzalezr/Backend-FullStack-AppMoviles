package com.appmovil.msvc.pedidos.services;

import com.appmovil.msvc.pedidos.clients.PagoClientRest;
import com.appmovil.msvc.pedidos.clients.ProductoClientRest;
import com.appmovil.msvc.pedidos.clients.UsuarioClientRest;
import com.appmovil.msvc.pedidos.dtos.PedidoCreationDTO;
import com.appmovil.msvc.pedidos.dtos.PedidoDTO;
import com.appmovil.msvc.pedidos.dtos.PedidoDetalleDTO;
import com.appmovil.msvc.pedidos.exceptions.PedidoException;
import com.appmovil.msvc.pedidos.exceptions.ResourceNotFoundException;
import com.appmovil.msvc.pedidos.model.Producto;
import com.appmovil.msvc.pedidos.model.Usuario;
import com.appmovil.msvc.pedidos.model.entity.Pedido;
import com.appmovil.msvc.pedidos.model.entity.PedidoDetalle;
import com.appmovil.msvc.pedidos.repositories.PedidoRepository;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoService - Unit Tests")
class PedidoServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UsuarioClientRest usuarioClientRest;

    @Mock
    private ProductoClientRest productoClientRest;

    @Mock
    private PagoClientRest pagoClientRest;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private Usuario usuarioTest;
    private Producto productoTest;
    private Pedido pedidoTest;
    private PedidoCreationDTO creationDTO;

    @BeforeEach
    void setUp() {
        // Usuario mock
        usuarioTest = new Usuario();
        usuarioTest.setIdUsuario(1L);
        usuarioTest.setNombre("Test");
        usuarioTest.setApellido("User");
        usuarioTest.setEmail("test@email.com");
        usuarioTest.setActivo(true);

        // Producto mock
        productoTest = new Producto();
        productoTest.setIdProducto(1L);
        productoTest.setNombre("Producto Test");
        productoTest.setPrecio(100);
        productoTest.setStock(50);
        productoTest.setActivo(true);

        // Pedido mock
        pedidoTest = new Pedido();
        pedidoTest.setId(1L);
        pedidoTest.setIdUsuario(1L);
        pedidoTest.setEstado("PENDIENTE");
        pedidoTest.setFechaPedido(LocalDateTime.now());
        pedidoTest.setDireccionEnvio("Test Address 123");
        pedidoTest.setMetodoPago("TARJETA");
        pedidoTest.setDetalles(new ArrayList<>());

        // Creation DTO
        creationDTO = new PedidoCreationDTO();
        creationDTO.setIdUsuario(1L);
        creationDTO.setDireccionEnvio("Test Address 123");
        creationDTO.setMetodoPago("TARJETA");
        creationDTO.setNotas("Test notes");
        
        PedidoDetalleDTO detalleDTO = new PedidoDetalleDTO();
        detalleDTO.setIdProducto(1L);
        detalleDTO.setCantidad(2);
        creationDTO.setDetalles(Arrays.asList(detalleDTO));
    }

    @Test
    @DisplayName("crearPedido - Debe crear pedido exitosamente con usuario y producto válidos")
    void testCrearPedido_ConDatosValidos_DebeCrearExitosamente() {
        // Given
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);
        when(productoClientRest.updateStock(eq(1L), eq(-2))).thenReturn(productoTest);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setId(1L);
            return pedido;
        });

        // When
        PedidoDTO resultado = pedidoService.crearPedido(creationDTO);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdUsuario()).isEqualTo(1L);
        assertThat(resultado.getEstado()).isEqualTo("PENDIENTE");
        assertThat(resultado.getDireccionEnvio()).isEqualTo("Test Address 123");
        assertThat(resultado.getDetalles()).hasSize(1);
        
        verify(usuarioClientRest).findById(1L);
        verify(productoClientRest).findById(1L);
        verify(productoClientRest).updateStock(1L, -2);
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("crearPedido - Debe lanzar excepción cuando usuario no existe")
    void testCrearPedido_ConUsuarioInexistente_DebeLanzarExcepcion() {
        // Given
        when(usuarioClientRest.findById(1L)).thenThrow(FeignException.class);

        // When & Then
        assertThatThrownBy(() -> pedidoService.crearPedido(creationDTO))
                .isInstanceOf(PedidoException.class)
                .hasMessageContaining("usuario");
        
        verify(usuarioClientRest).findById(1L);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPedido - Debe lanzar excepción cuando usuario está inactivo")
    void testCrearPedido_ConUsuarioInactivo_DebeLanzarExcepcion() {
        // Given
        usuarioTest.setActivo(false);
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);

        // When & Then
        assertThatThrownBy(() -> pedidoService.crearPedido(creationDTO))
                .isInstanceOf(PedidoException.class)
                .hasMessageContaining("inactivo");
        
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPedido - Debe lanzar excepción cuando producto no existe")
    void testCrearPedido_ConProductoInexistente_DebeLanzarExcepcion() {
        // Given
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenThrow(FeignException.class);

        // When & Then
        assertThatThrownBy(() -> pedidoService.crearPedido(creationDTO))
                .isInstanceOf(PedidoException.class)
                .hasMessageContaining("producto");
        
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPedido - Debe lanzar excepción cuando producto está inactivo")
    void testCrearPedido_ConProductoInactivo_DebeLanzarExcepcion() {
        // Given
        productoTest.setActivo(false);
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When & Then
        assertThatThrownBy(() -> pedidoService.crearPedido(creationDTO))
                .isInstanceOf(PedidoException.class)
                .hasMessageContaining("no está activo");
        
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPedido - Debe lanzar excepción cuando stock insuficiente")
    void testCrearPedido_ConStockInsuficiente_DebeLanzarExcepcion() {
        // Given
        productoTest.setStock(1); // Stock menor que cantidad solicitada (2)
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When & Then
        assertThatThrownBy(() -> pedidoService.crearPedido(creationDTO))
                .isInstanceOf(PedidoException.class)
                .hasMessageContaining("Stock insuficiente");
        
        verify(pedidoRepository, never()).save(any());
        verify(productoClientRest, never()).updateStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("crearPedido - Debe actualizar stock correctamente")
    void testCrearPedido_DebeActualizarStock() {
        // Given
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);
        when(productoClientRest.updateStock(anyLong(), anyInt())).thenReturn(productoTest);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        pedidoService.crearPedido(creationDTO);

        // Then
        verify(productoClientRest).updateStock(1L, -2); // Debe reducir 2 unidades
    }

    @Test
    @DisplayName("actualizarEstado - Debe actualizar estado del pedido")
    void testActualizarEstado_DebeActualizarExitosamente() {
        // Given
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoTest));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        PedidoDTO resultado = pedidoService.actualizarEstado(1L, "ENVIADO");

        // Then
        assertThat(resultado.getEstado()).isEqualTo("ENVIADO");
        verify(pedidoRepository).save(argThat(pedido -> pedido.getEstado().equals("ENVIADO")));
    }

    @Test
    @DisplayName("actualizarEstado - Debe lanzar excepción cuando pedido no existe")
    void testActualizarEstado_ConPedidoInexistente_DebeLanzarExcepcion() {
        // Given
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> pedidoService.actualizarEstado(999L, "ENVIADO"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("cancelarPedido - Debe cancelar pedido y restaurar stock")
    void testCancelarPedido_DebeCancelarYRestaurarStock() {
        // Given
        PedidoDetalle detalle = new PedidoDetalle();
        detalle.setIdProducto(1L);
        detalle.setCantidad(3);
        detalle.setPrecioUnitario(100);
        pedidoTest.getDetalles().add(detalle);
        
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoTest));
        when(productoClientRest.updateStock(anyLong(), anyInt())).thenReturn(productoTest);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        pedidoService.cancelarPedido(1L);

        // Then
        verify(productoClientRest).updateStock(1L, 3); // Debe restaurar 3 unidades
        verify(pedidoRepository).save(argThat(pedido -> pedido.getEstado().equals("CANCELADO")));
    }

    @Test
    @DisplayName("cancelarPedido - Debe lanzar excepción si pedido no está PENDIENTE")
    void testCancelarPedido_ConEstadoNoPendiente_DebeLanzarExcepcion() {
        // Given
        pedidoTest.setEstado("ENVIADO");
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoTest));

        // When & Then
        assertThatThrownBy(() -> pedidoService.cancelarPedido(1L))
                .isInstanceOf(PedidoException.class)
                .hasMessageContaining("PENDIENTE");
        
        verify(productoClientRest, never()).updateStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("findById - Debe retornar pedido con datos de usuario enriquecidos")
    void testFindById_DebeRetornarPedidoEnriquecido() {
        // Given
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);

        // When
        PedidoDTO resultado = pedidoService.findById(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdUsuario()).isEqualTo(1L);
        assertThat(resultado.getNombreUsuario()).isEqualTo("Test User");
        assertThat(resultado.getEmailUsuario()).isEqualTo("test@email.com");
        
        verify(usuarioClientRest).findById(1L);
    }

    @Test
    @DisplayName("findById - Debe funcionar aunque falle obtener datos de usuario")
    void testFindById_SiFallaUsuario_DebeRetornarPedidoSinDatosUsuario() {
        // Given
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoTest));
        when(usuarioClientRest.findById(1L)).thenThrow(FeignException.class);

        // When
        PedidoDTO resultado = pedidoService.findById(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdUsuario()).isEqualTo(1L);
        assertThat(resultado.getNombreUsuario()).isNull();
        assertThat(resultado.getEmailUsuario()).isNull();
    }

    @Test
    @DisplayName("findByUsuario - Debe retornar pedidos ordenados por fecha descendente")
    void testFindByUsuario_DebeRetornarPedidosOrdenados() {
        // Given
        Pedido pedido1 = new Pedido();
        pedido1.setId(1L);
        pedido1.setIdUsuario(1L);
        pedido1.setEstado("PENDIENTE");
        pedido1.setFechaPedido(LocalDateTime.now().minusDays(2));
        pedido1.setDetalles(new ArrayList<>());

        Pedido pedido2 = new Pedido();
        pedido2.setId(2L);
        pedido2.setIdUsuario(1L);
        pedido2.setEstado("ENVIADO");
        pedido2.setFechaPedido(LocalDateTime.now().minusDays(1));
        pedido2.setDetalles(new ArrayList<>());

        when(pedidoRepository.findByIdUsuarioOrderByFechaPedidoDesc(1L))
                .thenReturn(Arrays.asList(pedido2, pedido1)); // Más reciente primero
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);

        // When
        List<PedidoDTO> resultado = pedidoService.findByUsuario(1L);

        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getIdPedido()).isEqualTo(2L);
        assertThat(resultado.get(1).getIdPedido()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByEstado - Debe retornar solo pedidos del estado especificado")
    void testFindByEstado_DebeRetornarPedidosFiltrados() {
        // Given
        when(pedidoRepository.findByEstado("PENDIENTE")).thenReturn(Arrays.asList(pedidoTest));
        when(usuarioClientRest.findById(anyLong())).thenReturn(usuarioTest);

        // When
        List<PedidoDTO> resultado = pedidoService.findByEstado("PENDIENTE");

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getEstado()).isEqualTo("PENDIENTE");
        verify(pedidoRepository).findByEstado("PENDIENTE");
    }

    @Test
    @DisplayName("actualizarEstadoPago - Debe cambiar estado a PAGADO")
    void testActualizarEstadoPago_DebeActualizarAPagado() {
        // Given
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoTest));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        pedidoService.actualizarEstadoPago(1L);

        // Then
        verify(pedidoRepository).save(argThat(pedido -> pedido.getEstado().equals("PAGADO")));
    }

    @Test
    @DisplayName("crearPedido - Debe calcular totales correctamente")
    void testCrearPedido_DebeCalcularTotalesCorrectamente() {
        // Given
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);
        when(productoClientRest.updateStock(anyLong(), anyInt())).thenReturn(productoTest);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setId(1L);
            return pedido;
        });

        // When
        PedidoDTO resultado = pedidoService.crearPedido(creationDTO);

        // Then
        assertThat(resultado.getDetalles().get(0).getSubtotal()).isNotNull();
        assertThat(resultado.getSubtotal()).isNotNull();
        assertThat(resultado.getTotal()).isNotNull();
    }

    @Test
    @DisplayName("findAll - Debe retornar todos los pedidos")
    void testFindAll_DebeRetornarTodosPedidos() {
        // Given
        Pedido pedido2 = new Pedido();
        pedido2.setId(2L);
        pedido2.setIdUsuario(2L);
        pedido2.setEstado("ENVIADO");
        pedido2.setDetalles(new ArrayList<>());
        
        when(pedidoRepository.findAll()).thenReturn(Arrays.asList(pedidoTest, pedido2));
        when(usuarioClientRest.findById(anyLong())).thenReturn(usuarioTest);

        // When
        List<PedidoDTO> resultado = pedidoService.findAll();

        // Then
        assertThat(resultado).hasSize(2);
        verify(pedidoRepository).findAll();
    }
}
