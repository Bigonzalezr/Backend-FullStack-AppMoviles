package com.appmovil.msvc.carrito.services;

import com.appmovil.msvc.carrito.clients.ProductoClientRest;
import com.appmovil.msvc.carrito.clients.UsuarioClientRest;
import com.appmovil.msvc.carrito.dtos.CarritoDTO;
import com.appmovil.msvc.carrito.dtos.ItemCarritoDTO;
import com.appmovil.msvc.carrito.exceptions.CarritoException;
import com.appmovil.msvc.carrito.exceptions.ResourceNotFoundException;
import com.appmovil.msvc.carrito.model.Producto;
import com.appmovil.msvc.carrito.model.Usuario;
import com.appmovil.msvc.carrito.model.entity.Carrito;
import com.appmovil.msvc.carrito.model.entity.ItemCarrito;
import com.appmovil.msvc.carrito.repositories.CarritoRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CarritoService - Unit Tests")
class CarritoServiceImplTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private UsuarioClientRest usuarioClientRest;

    @Mock
    private ProductoClientRest productoClientRest;

    @InjectMocks
    private CarritoServiceImpl carritoService;

    private Usuario usuarioTest;
    private Producto productoTest;
    private Carrito carritoTest;

    @BeforeEach
    void setUp() {
        // Usuario mock
        usuarioTest = new Usuario();
        usuarioTest.setIdUsuario(1L);
        usuarioTest.setNombre("María");
        usuarioTest.setApellido("González");
        usuarioTest.setEmail("maria@email.com");
        usuarioTest.setActivo(true);

        // Producto mock
        productoTest = new Producto();
        productoTest.setIdProducto(1L);
        productoTest.setNombre("iPhone 15");
        productoTest.setPrecio(1000);
        productoTest.setStock(20);
        productoTest.setActivo(true);

        // Carrito mock
        carritoTest = new Carrito();
        carritoTest.setId(1L);
        carritoTest.setIdUsuario(1L);
        carritoTest.setActivo(true);
        carritoTest.setFechaCreacion(LocalDateTime.now());
        carritoTest.setItems(new ArrayList<>());
    }

    @Test
    @DisplayName("obtenerCarrito - Debe crear carrito si no existe")
    void testObtenerCarrito_CuandoNoExiste_DebeCrearNuevo() {
        // Given
        when(carritoRepository.findByIdUsuarioAndActivo(1L, true)).thenReturn(Optional.empty());
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> {
            Carrito c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        // When
        CarritoDTO resultado = carritoService.obtenerCarrito(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdUsuario()).isEqualTo(1L);
        assertThat(resultado.isActivo()).isTrue();
        assertThat(resultado.getItems()).isEmpty();
        
        verify(carritoRepository).save(any(Carrito.class));
    }

    @Test
    @DisplayName("obtenerCarrito - Debe retornar carrito existente")
    void testObtenerCarrito_CuandoExiste_DebeRetornarExistente() {
        // Given
        when(carritoRepository.findByIdUsuarioAndActivo(1L, true)).thenReturn(Optional.of(carritoTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);

        // When
        CarritoDTO resultado = carritoService.obtenerCarrito(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdCarrito()).isEqualTo(1L);
        assertThat(resultado.getIdUsuario()).isEqualTo(1L);
        
        verify(carritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("agregarItem - Debe agregar item nuevo exitosamente")
    void testAgregarItem_ItemNuevo_DebeAgregarExitosamente() {
        // Given
        when(carritoRepository.findByIdUsuarioAndActivo(1L, true)).thenReturn(Optional.of(carritoTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        CarritoDTO resultado = carritoService.agregarItem(1L, 1L, 3);

        // Then
        assertThat(resultado.getItems()).hasSize(1);
        assertThat(resultado.getItems().get(0).getIdProducto()).isEqualTo(1L);
        assertThat(resultado.getItems().get(0).getCantidad()).isEqualTo(3);
        
        verify(carritoRepository).save(any(Carrito.class));
    }

    @Test
    @DisplayName("agregarItem - Debe incrementar cantidad si item existe")
    void testAgregarItem_ItemExistente_DebeIncrementarCantidad() {
        // Given
        ItemCarrito itemExistente = new ItemCarrito();
        itemExistente.setId(1L);
        itemExistente.setCarrito(carritoTest);
        itemExistente.setIdProducto(1L);
        itemExistente.setCantidad(2);
        itemExistente.setPrecioUnitario(1000);
        carritoTest.getItems().add(itemExistente);

        when(carritoRepository.findByIdUsuarioAndActivo(1L, true)).thenReturn(Optional.of(carritoTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        CarritoDTO resultado = carritoService.agregarItem(1L, 1L, 3);

        // Then
        assertThat(resultado.getItems()).hasSize(1);
        assertThat(resultado.getItems().get(0).getCantidad()).isEqualTo(5); // 2 + 3
    }

    @Test
    @DisplayName("agregarItem - Debe lanzar excepción con cantidad mayor a 10")
    void testAgregarItem_CantidadMayorA10_DebeLanzarExcepcion() {
        // Given
        when(carritoRepository.findByIdUsuarioAndActivo(1L, true)).thenReturn(Optional.of(carritoTest));
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When & Then
        assertThatThrownBy(() -> carritoService.agregarItem(1L, 1L, 11))
                .isInstanceOf(CarritoException.class)
                .hasMessageContaining("10 unidades");
        
        verify(carritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("agregarItem - Debe lanzar excepción si total excede 10 unidades")
    void testAgregarItem_TotalExcede10_DebeLanzarExcepcion() {
        // Given
        ItemCarrito itemExistente = new ItemCarrito();
        itemExistente.setIdProducto(1L);
        itemExistente.setCantidad(8);
        carritoTest.getItems().add(itemExistente);

        when(carritoRepository.findByIdUsuarioAndActivo(1L, true)).thenReturn(Optional.of(carritoTest));
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When & Then
        assertThatThrownBy(() -> carritoService.agregarItem(1L, 1L, 5)) // 8 + 5 = 13 > 10
                .isInstanceOf(CarritoException.class)
                .hasMessageContaining("10 unidades");
    }

    @Test
    @DisplayName("agregarItem - Debe validar stock suficiente")
    void testAgregarItem_StockInsuficiente_DebeLanzarExcepcion() {
        // Given
        productoTest.setStock(2); // Stock menor que cantidad solicitada
        when(carritoRepository.findByIdUsuarioAndActivo(1L, true)).thenReturn(Optional.of(carritoTest));
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When & Then
        assertThatThrownBy(() -> carritoService.agregarItem(1L, 1L, 5))
                .isInstanceOf(CarritoException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    @DisplayName("agregarItem - Debe validar que producto esté activo")
    void testAgregarItem_ProductoInactivo_DebeLanzarExcepcion() {
        // Given
        productoTest.setActivo(false);
        when(carritoRepository.findByIdUsuarioAndActivo(1L, true)).thenReturn(Optional.of(carritoTest));
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When & Then
        assertThatThrownBy(() -> carritoService.agregarItem(1L, 1L, 2))
                .isInstanceOf(CarritoException.class)
                .hasMessageContaining("no está disponible");
    }

    @Test
    @DisplayName("agregarItem - Debe manejar error cuando producto no existe")
    void testAgregarItem_ProductoNoExiste_DebeLanzarExcepcion() {
        // Given
        when(carritoRepository.findByIdUsuarioAndActivo(1L, true)).thenReturn(Optional.of(carritoTest));
        when(productoClientRest.findById(1L)).thenThrow(mock(FeignException.class));

        // When & Then
        assertThatThrownBy(() -> carritoService.agregarItem(1L, 1L, 2))
                .isInstanceOf(CarritoException.class)
                .hasMessageContaining("producto");
    }

    @Test
    @DisplayName("actualizarCantidad - Debe actualizar cantidad del item")
    void testActualizarCantidad_DebeActualizarExitosamente() {
        // Given
        ItemCarrito item = new ItemCarrito();
        item.setId(1L);
        item.setCarrito(carritoTest);
        item.setIdProducto(1L);
        item.setCantidad(3);
        item.setPrecioUnitario(1000);
        carritoTest.getItems().add(item);

        when(carritoRepository.findByIdUsuarioAndActivo(1L, true)).thenReturn(Optional.of(carritoTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        CarritoDTO resultado = carritoService.actualizarCantidad(1L, 1L, 5);

        // Then
        assertThat(resultado.getItems().get(0).getCantidad()).isEqualTo(5);
        verify(carritoRepository).save(any());
    }

    @Test
    @DisplayName("actualizarCantidad - Debe lanzar excepción si item no existe")
    void testActualizarCantidad_ItemNoExiste_DebeLanzarExcepcion() {
        // Given
        when(carritoRepository.findByIdUsuarioAndActivo(1L, true)).thenReturn(Optional.of(carritoTest));

        // When & Then
        assertThatThrownBy(() -> carritoService.actualizarCantidad(1L, 999L, 5))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Item");
    }

    @Test
    @DisplayName("removerItem - Debe remover item del carrito")
    void testRemoverItem_DebeRemoverExitosamente() {
        // Given
        ItemCarrito item = new ItemCarrito();
        item.setId(1L);
        item.setIdProducto(1L);
        item.setCantidad(3);
        carritoTest.getItems().add(item);

        when(carritoRepository.findByIdUsuarioAndActivo(1L, true)).thenReturn(Optional.of(carritoTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        CarritoDTO resultado = carritoService.removerItem(1L, 1L);

        // Then
        assertThat(resultado.getItems()).isEmpty();
        verify(carritoRepository).save(any());
    }

    @Test
    @DisplayName("removerItem - Debe lanzar excepción si item no existe")
    void testRemoverItem_ItemNoExiste_DebeLanzarExcepcion() {
        // Given
        when(carritoRepository.findByIdUsuarioAndActivo(1L, true)).thenReturn(Optional.of(carritoTest));

        // When & Then
        assertThatThrownBy(() -> carritoService.removerItem(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Item");
    }

    @Test
    @DisplayName("vaciarCarrito - Debe limpiar todos los items")
    void testVaciarCarrito_DebeLimpiarItems() {
        // Given
        ItemCarrito item1 = new ItemCarrito();
        item1.setIdProducto(1L);
        ItemCarrito item2 = new ItemCarrito();
        item2.setIdProducto(2L);
        carritoTest.getItems().addAll(Arrays.asList(item1, item2));

        when(carritoRepository.findByIdUsuarioAndActivo(1L, true)).thenReturn(Optional.of(carritoTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        CarritoDTO resultado = carritoService.vaciarCarrito(1L);

        // Then
        assertThat(resultado.getItems()).isEmpty();
        verify(carritoRepository).save(argThat(c -> c.getItems().isEmpty()));
    }

    @Test
    @DisplayName("obtenerCarrito - Debe enriquecer con datos de productos")
    void testObtenerCarrito_DebeEnriquecerConProductos() {
        // Given
        ItemCarrito item = new ItemCarrito();
        item.setIdProducto(1L);
        item.setCantidad(2);
        item.setPrecioUnitario(1000);
        carritoTest.getItems().add(item);

        when(carritoRepository.findByIdUsuarioAndActivo(1L, true)).thenReturn(Optional.of(carritoTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When
        CarritoDTO resultado = carritoService.obtenerCarrito(1L);

        // Then
        assertThat(resultado.getItems()).hasSize(1);
        assertThat(resultado.getItems().get(0).getNombreProducto()).isEqualTo("iPhone 15");
        verify(productoClientRest).findById(1L);
    }

    @Test
    @DisplayName("obtenerCarrito - Debe calcular totales correctamente")
    void testObtenerCarrito_DebeCalcularTotales() {
        // Given
        ItemCarrito item1 = new ItemCarrito();
        item1.setIdProducto(1L);
        item1.setCantidad(2);
        item1.setPrecioUnitario(1000);
        item1.setSubtotal(2000);

        ItemCarrito item2 = new ItemCarrito();
        item2.setIdProducto(2L);
        item2.setCantidad(3);
        item2.setPrecioUnitario(500);
        item2.setSubtotal(1500);

        carritoTest.getItems().addAll(Arrays.asList(item1, item2));

        Producto producto2 = new Producto();
        producto2.setIdProducto(2L);
        producto2.setNombre("Producto 2");
        producto2.setActivo(true);

        when(carritoRepository.findByIdUsuarioAndActivo(1L, true)).thenReturn(Optional.of(carritoTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);
        when(productoClientRest.findById(2L)).thenReturn(producto2);

        // When
        CarritoDTO resultado = carritoService.obtenerCarrito(1L);

        // Then
        assertThat(resultado.getTotal()).isEqualTo(3500); // 2000 + 1500
        assertThat(resultado.getTotalItems()).isEqualTo(5); // 2 + 3
    }

    @Test
    @DisplayName("obtenerCarrito - Debe manejar error al obtener datos de usuario")
    void testObtenerCarrito_ErrorUsuario_DebeRetornarSinDatosUsuario() {
        // Given
        when(carritoRepository.findByIdUsuarioAndActivo(1L, true)).thenReturn(Optional.of(carritoTest));
        when(usuarioClientRest.findById(1L)).thenThrow(mock(FeignException.class));

        // When
        CarritoDTO resultado = carritoService.obtenerCarrito(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombreUsuario()).isNull();
    }

    @Test
    @DisplayName("agregarItem - Debe calcular subtotal correctamente")
    void testAgregarItem_DebeCalcularSubtotal() {
        // Given
        when(carritoRepository.findByIdUsuarioAndActivo(1L, true)).thenReturn(Optional.of(carritoTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        CarritoDTO resultado = carritoService.agregarItem(1L, 1L, 3);

        // Then
        ItemCarritoDTO itemDTO = resultado.getItems().get(0);
        assertThat(itemDTO.getSubtotal()).isEqualTo(3000); // 3 * 1000
    }
}
