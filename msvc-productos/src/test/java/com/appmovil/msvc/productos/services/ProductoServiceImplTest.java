package com.appmovil.msvc.productos.services;

import com.appmovil.msvc.productos.dtos.ProductoUpdateDTO;
import com.appmovil.msvc.productos.exception.ProductoException;
import com.appmovil.msvc.productos.models.entities.Producto;
import com.appmovil.msvc.productos.repositories.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductoService - Unit Tests")
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Producto productoTest;
    private ProductoUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        productoTest = Producto.builder()
                .id(1L)
                .nombre("MacBook Pro")
                .descripcion("Laptop profesional Apple")
                .precio(2500)
                .stock(10)
                .categoria("LAPTOPS")
                .activo(true)
                .rating(4.5)
                .build();

        updateDTO = new ProductoUpdateDTO();
        updateDTO.setNombre("MacBook Pro M2");
        updateDTO.setDescripcion("Nueva versión con M2");
        updateDTO.setPrecio(2800);
        updateDTO.setStock(15);
        updateDTO.setCategoria("LAPTOPS");
        updateDTO.setImagen("imagen.jpg");
        updateDTO.setRating(4.7);
    }

    @Test
    @DisplayName("findAll - Debe retornar lista de todos los productos")
    void testFindAll_DebeRetornarLista() {
        // Given
        Producto producto2 = Producto.builder()
                .id(2L)
                .nombre("Dell XPS")
                .precio(2000)
                .categoria("LAPTOPS")
                .stock(5)
                .activo(true)
                .build();

        when(productoRepository.findAll()).thenReturn(Arrays.asList(productoTest, producto2));

        // When
        List<Producto> resultado = productoService.findAll();

        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNombre()).isEqualTo("MacBook Pro");
        assertThat(resultado.get(1).getNombre()).isEqualTo("Dell XPS");
        verify(productoRepository).findAll();
    }

    @Test
    @DisplayName("findById - Debe retornar producto cuando existe")
    void testFindById_ConIdExistente_DebeRetornarProducto() {
        // Given
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoTest));

        // When
        Producto resultado = productoService.findById(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("MacBook Pro");
        verify(productoRepository).findById(1L);
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando producto no existe")
    void testFindById_ConIdInexistente_DebeLanzarExcepcion() {
        // Given
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productoService.findById(999L))
                .isInstanceOf(ProductoException.class)
                .hasMessageContaining("999");
        
        verify(productoRepository).findById(999L);
    }

    @Test
    @DisplayName("save - Debe guardar producto exitosamente")
    void testSave_DebeGuardarExitosamente() {
        // Given
        Producto nuevoProducto = Producto.builder()
                .nombre("iPad Pro")
                .precio(1200)
                .categoria("TABLETS")
                .stock(8)
                .build();

        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId(3L);
            return p;
        });

        // When
        Producto resultado = productoService.save(nuevoProducto);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(3L);
        verify(productoRepository).save(nuevoProducto);
    }

    @Test
    @DisplayName("update - Debe actualizar producto exitosamente")
    void testUpdate_ConDatosValidos_DebeActualizarExitosamente() {
        // Given
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoTest));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Producto resultado = productoService.update(1L, updateDTO);

        // Then
        assertThat(resultado.getNombre()).isEqualTo("MacBook Pro M2");
        assertThat(resultado.getPrecio()).isEqualTo(2800);
        assertThat(resultado.getStock()).isEqualTo(15);
        assertThat(resultado.getRating()).isEqualTo(4.7);
        
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    @DisplayName("update - Debe lanzar excepción si producto no existe")
    void testUpdate_ConIdInexistente_DebeLanzarExcepcion() {
        // Given
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productoService.update(999L, updateDTO))
                .isInstanceOf(ProductoException.class)
                .hasMessageContaining("999");
        
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe actualizar solo campos proporcionados")
    void testUpdate_DebeMantenerRatingExistente() {
        // Given
        ProductoUpdateDTO updateSinRating = new ProductoUpdateDTO();
        updateSinRating.setNombre("Nuevo Nombre");
        updateSinRating.setPrecio(3000);
        updateSinRating.setCategoria("LAPTOPS");
        updateSinRating.setStock(20);
        updateSinRating.setRating(null); // No actualizar rating

        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoTest));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Producto resultado = productoService.update(1L, updateSinRating);

        // Then
        assertThat(resultado.getRating()).isEqualTo(4.5); // Mantiene rating original
    }

    @Test
    @DisplayName("delete - Debe eliminar producto exitosamente")
    void testDelete_DebeEliminarExitosamente() {
        // Given
        when(productoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productoRepository).deleteById(1L);

        // When
        productoService.delete(1L);

        // Then
        verify(productoRepository).existsById(1L);
        verify(productoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción si producto no existe")
    void testDelete_ConIdInexistente_DebeLanzarExcepcion() {
        // Given
        when(productoRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> productoService.delete(999L))
                .isInstanceOf(ProductoException.class)
                .hasMessageContaining("999");
        
        verify(productoRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("findActivos - Debe retornar solo productos activos")
    void testFindActivos_DebeRetornarSoloActivos() {
        // Given
        Producto inactivo = Producto.builder()
                .id(2L)
                .nombre("Producto Inactivo")
                .activo(false)
                .build();

        when(productoRepository.findByActivo(true)).thenReturn(Arrays.asList(productoTest));

        // When
        List<Producto> resultado = productoService.findActivos();

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getActivo()).isTrue();
        verify(productoRepository).findByActivo(true);
    }

    @Test
    @DisplayName("findByCategoria - Debe retornar productos de la categoría especificada")
    void testFindByCategoria_DebeRetornarPorCategoria() {
        // Given
        when(productoRepository.findByCategoriaIgnoreCaseAndActivo("LAPTOPS", true))
                .thenReturn(Arrays.asList(productoTest));

        // When
        List<Producto> resultado = productoService.findByCategoria("LAPTOPS");

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCategoria()).isEqualTo("LAPTOPS");
        verify(productoRepository).findByCategoriaIgnoreCaseAndActivo("LAPTOPS", true);
    }

    @Test
    @DisplayName("findByCategoria - Con 'todos' debe retornar todos los activos")
    void testFindByCategoria_Todos_DebeRetornarTodosActivos() {
        // Given
        when(productoRepository.findByActivo(true)).thenReturn(Arrays.asList(productoTest));

        // When
        List<Producto> resultado = productoService.findByCategoria("todos");

        // Then
        assertThat(resultado).hasSize(1);
        verify(productoRepository).findByActivo(true);
        verify(productoRepository, never()).findByCategoriaIgnoreCaseAndActivo(anyString(), anyBoolean());
    }

    @Test
    @DisplayName("buscarPorNombre - Debe buscar productos por nombre")
    void testBuscarPorNombre_DebeRetornarProductosCoincidentes() {
        // Given
        when(productoRepository.findByNombreContainingIgnoreCase("macbook"))
                .thenReturn(Arrays.asList(productoTest));

        // When
        List<Producto> resultado = productoService.buscarPorNombre("macbook");

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).containsIgnoringCase("macbook");
        verify(productoRepository).findByNombreContainingIgnoreCase("macbook");
    }

    @Test
    @DisplayName("actualizarStock - Debe actualizar stock correctamente")
    void testActualizarStock_DebeActualizarExitosamente() {
        // Given
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoTest));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Producto resultado = productoService.actualizarStock(1L, -3);

        // Then
        assertThat(resultado.getStock()).isEqualTo(7); // 10 - 3
        verify(productoRepository).save(argThat(p -> p.getStock() == 7));
    }

    @Test
    @DisplayName("actualizarStock - Debe permitir incrementar stock")
    void testActualizarStock_Incrementar_DebeAumentarStock() {
        // Given
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoTest));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Producto resultado = productoService.actualizarStock(1L, 5);

        // Then
        assertThat(resultado.getStock()).isEqualTo(15); // 10 + 5
    }

    @Test
    @DisplayName("actualizarStock - Debe lanzar excepción si stock resultante es negativo")
    void testActualizarStock_StockNegativo_DebeLanzarExcepcion() {
        // Given
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoTest));

        // When & Then
        assertThatThrownBy(() -> productoService.actualizarStock(1L, -15))
                .isInstanceOf(ProductoException.class)
                .hasMessageContaining("Stock insuficiente");
        
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizarStock - Debe lanzar excepción si producto no existe")
    void testActualizarStock_ProductoNoExiste_DebeLanzarExcepcion() {
        // Given
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productoService.actualizarStock(999L, 5))
                .isInstanceOf(ProductoException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("buscarPorNombre - Debe retornar lista vacía si no hay coincidencias")
    void testBuscarPorNombre_SinCoincidencias_DebeRetornarListaVacia() {
        // Given
        when(productoRepository.findByNombreContainingIgnoreCase("inexistente"))
                .thenReturn(Arrays.asList());

        // When
        List<Producto> resultado = productoService.buscarPorNombre("inexistente");

        // Then
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findByCategoria - Debe ignorar mayúsculas/minúsculas")
    void testFindByCategoria_IgnoraCaseSensitive() {
        // Given
        when(productoRepository.findByCategoriaIgnoreCaseAndActivo("laptops", true))
                .thenReturn(Arrays.asList(productoTest));

        // When
        List<Producto> resultado = productoService.findByCategoria("laptops");

        // Then
        assertThat(resultado).hasSize(1);
        verify(productoRepository).findByCategoriaIgnoreCaseAndActivo("laptops", true);
    }
}
