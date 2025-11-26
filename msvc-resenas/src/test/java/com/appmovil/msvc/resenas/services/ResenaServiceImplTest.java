package com.appmovil.msvc.resenas.services;

import com.appmovil.msvc.resenas.clients.ProductoClientRest;
import com.appmovil.msvc.resenas.clients.UsuarioClientRest;
import com.appmovil.msvc.resenas.dtos.ResenaDTO;
import com.appmovil.msvc.resenas.exceptions.ResenaException;
import com.appmovil.msvc.resenas.models.Producto;
import com.appmovil.msvc.resenas.models.Usuario;
import com.appmovil.msvc.resenas.models.entities.Resena;
import com.appmovil.msvc.resenas.repositories.ResenaRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResenaService - Unit Tests")
class ResenaServiceImplTest {

    @Mock
    private ResenaRepository ResenaRepository;

    @Mock
    private UsuarioClientRest usuarioClientRest;

    @Mock
    private ProductoClientRest productoClientRest;

    @InjectMocks
    private ResenaServiceImpl ResenaService;

    private Resena ResenaTest;
    private Usuario usuarioTest;
    private Producto productoTest;

    @BeforeEach
    void setUp() {
        usuarioTest = new Usuario();
        usuarioTest.setIdUsuario(1L);
        usuarioTest.setNombre("Juan Pérez");
        usuarioTest.setActivo(true);

        productoTest = new Producto();
        productoTest.setIdProducto(1L);
        productoTest.setNombre("iPhone 15");
        productoTest.setActivo(true);

        ResenaTest = new Resena();
        ResenaTest.setId(1L);
        ResenaTest.setIdUsuario(1L);
        ResenaTest.setIdProducto(1L);
        ResenaTest.setRating(5);
        ResenaTest.setComentario("Excelente producto, muy recomendado");
        ResenaTest.setFechaCreacion(LocalDateTime.now());
        ResenaTest.setActivo(true);
    }

    @Test
    @DisplayName("findAll - Debe retornar lista de Resenas enriquecidas")
    void testFindAll_DebeRetornarListaEnriquecida() {
        // Given
        when(ResenaRepository.findAll()).thenReturn(Arrays.asList(ResenaTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When
        List<ResenaDTO> resultado = ResenaService.findAll();

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombreUsuario()).isEqualTo("Juan Pérez");
        assertThat(resultado.get(0).getNombreProducto()).isEqualTo("iPhone 15");
        assertThat(resultado.get(0).getRating()).isEqualTo(5);
        verify(ResenaRepository).findAll();
        verify(usuarioClientRest).findById(1L);
        verify(productoClientRest).findById(1L);
    }

    @Test
    @DisplayName("findById - Debe retornar Resena cuando existe")
    void testFindById_ConIdExistente_DebeRetornarResena() {
        // Given
        when(ResenaRepository.findById(1L)).thenReturn(Optional.of(ResenaTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When
        ResenaDTO resultado = ResenaService.findById(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getComentario()).isEqualTo("Excelente producto, muy recomendado");
        verify(ResenaRepository).findById(1L);
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando Resena no existe")
    void testFindById_ConIdInexistente_DebeLanzarExcepcion() {
        // Given
        when(ResenaRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ResenaService.findById(999L))
                .isInstanceOf(ResenaException.class)
                .hasMessageContaining("999");
        
        verify(ResenaRepository).findById(999L);
    }

    @Test
    @DisplayName("save - Debe guardar Resena exitosamente")
    void testSave_ConDatosValidos_DebeGuardarExitosamente() {
        // Given
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);
        when(ResenaRepository.existsByIdUsuarioAndIdProducto(1L, 1L)).thenReturn(false);
        when(ResenaRepository.save(any(Resena.class))).thenAnswer(inv -> {
            Resena r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // When
        ResenaDTO resultado = ResenaService.save(ResenaTest);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getRating()).isEqualTo(5);
        verify(usuarioClientRest).findById(1L);
        verify(productoClientRest).findById(1L);
        verify(ResenaRepository).existsByIdUsuarioAndIdProducto(1L, 1L);
        verify(ResenaRepository).save(any(Resena.class));
    }

    @Test
    @DisplayName("save - Debe lanzar excepción cuando usuario no existe")
    void testSave_ConUsuarioInexistente_DebeLanzarExcepcion() {
        // Given
        when(usuarioClientRest.findById(1L)).thenThrow(mock(FeignException.class));

        // When & Then
        assertThatThrownBy(() -> ResenaService.save(ResenaTest))
                .isInstanceOf(ResenaException.class)
                .hasMessageContaining("Usuario no existe");
        
        verify(ResenaRepository, never()).save(any());
    }

    @Test
    @DisplayName("save - Debe lanzar excepción cuando producto no existe")
    void testSave_ConProductoInexistente_DebeLanzarExcepcion() {
        // Given
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenThrow(mock(FeignException.class));

        // When & Then
        assertThatThrownBy(() -> ResenaService.save(ResenaTest))
                .isInstanceOf(ResenaException.class)
                .hasMessageContaining("Producto no existe");
        
        verify(ResenaRepository, never()).save(any());
    }

    @Test
    @DisplayName("save - Debe lanzar excepción cuando ya existe Resena del usuario para el producto")
    void testSave_ConResenaDuplicada_DebeLanzarExcepcion() {
        // Given
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);
        when(ResenaRepository.existsByIdUsuarioAndIdProducto(1L, 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> ResenaService.save(ResenaTest))
                .isInstanceOf(ResenaException.class)
                .hasMessageContaining("Ya existe una Resena");
        
        verify(ResenaRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe actualizar Resena exitosamente")
    void testUpdate_ConDatosValidos_DebeActualizarExitosamente() {
        // Given
        Resena ResenaActualizada = new Resena();
        ResenaActualizada.setRating(4);
        ResenaActualizada.setComentario("Actualizado: Buen producto pero mejorable");

        when(ResenaRepository.findById(1L)).thenReturn(Optional.of(ResenaTest));
        when(ResenaRepository.save(any(Resena.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When
        ResenaDTO resultado = ResenaService.update(1L, ResenaActualizada);

        // Then
        assertThat(resultado.getRating()).isEqualTo(4);
        assertThat(resultado.getComentario()).contains("Actualizado");
        verify(ResenaRepository).save(argThat(r -> 
            r.getRating() == 4 && r.getComentario().contains("Actualizado")
        ));
    }

    @Test
    @DisplayName("update - Debe lanzar excepción si Resena no existe")
    void testUpdate_ConIdInexistente_DebeLanzarExcepcion() {
        // Given
        when(ResenaRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ResenaService.update(999L, ResenaTest))
                .isInstanceOf(ResenaException.class)
                .hasMessageContaining("999");
        
        verify(ResenaRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete - Debe desactivar Resena (soft delete)")
    void testDelete_DebeDesactivarResena() {
        // Given
        when(ResenaRepository.findById(1L)).thenReturn(Optional.of(ResenaTest));
        when(ResenaRepository.save(any(Resena.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        ResenaService.delete(1L);

        // Then
        verify(ResenaRepository).save(argThat(r -> !r.getActivo()));
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción si Resena no existe")
    void testDelete_ConIdInexistente_DebeLanzarExcepcion() {
        // Given
        when(ResenaRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ResenaService.delete(999L))
                .isInstanceOf(ResenaException.class);
        
        verify(ResenaRepository, never()).save(any());
    }

    @Test
    @DisplayName("findByUsuario - Debe retornar Resenas del usuario")
    void testFindByUsuario_DebeRetornarResenasDelUsuario() {
        // Given
        when(ResenaRepository.findByIdUsuario(1L)).thenReturn(Arrays.asList(ResenaTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When
        List<ResenaDTO> resultado = ResenaService.findByUsuario(1L);

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdUsuario()).isEqualTo(1L);
        verify(ResenaRepository).findByIdUsuario(1L);
    }

    @Test
    @DisplayName("findByProducto - Debe retornar solo Resenas activas del producto")
    void testFindByProducto_DebeRetornarSoloResenasActivas() {
        // Given
        when(ResenaRepository.findByIdProductoAndActivo(1L, true)).thenReturn(Arrays.asList(ResenaTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When
        List<ResenaDTO> resultado = ResenaService.findByProducto(1L);

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getActivo()).isTrue();
        assertThat(resultado.get(0).getIdProducto()).isEqualTo(1L);
        verify(ResenaRepository).findByIdProductoAndActivo(1L, true);
    }

    @Test
    @DisplayName("getAverageRatingByProducto - Debe calcular promedio correcto")
    void testGetAverageRatingByProducto_DebeCalcularPromedio() {
        // Given
        when(ResenaRepository.findAverageRatingByProducto(1L)).thenReturn(4.5);

        // When
        Double promedio = ResenaService.getAverageRatingByProducto(1L);

        // Then
        assertThat(promedio).isEqualTo(4.5);
        verify(ResenaRepository).findAverageRatingByProducto(1L);
    }

    @Test
    @DisplayName("getAverageRatingByProducto - Debe retornar 0.0 si no hay Resenas")
    void testGetAverageRatingByProducto_SinResenas_DebeRetornar0() {
        // Given
        when(ResenaRepository.findAverageRatingByProducto(1L)).thenReturn(null);

        // When
        Double promedio = ResenaService.getAverageRatingByProducto(1L);

        // Then
        assertThat(promedio).isEqualTo(0.0);
    }

    @Test
    @DisplayName("findAll - Debe lanzar excepción si falla obtener usuario")
    void testFindAll_ErrorUsuario_DebeLanzarExcepcion() {
        // Given
        when(ResenaRepository.findAll()).thenReturn(Arrays.asList(ResenaTest));
        when(usuarioClientRest.findById(1L)).thenThrow(mock(FeignException.class));

        // When & Then
        assertThatThrownBy(() -> ResenaService.findAll())
                .isInstanceOf(ResenaException.class)
                .hasMessageContaining("usuario no existe");
    }

    @Test
    @DisplayName("findAll - Debe lanzar excepción si falla obtener producto")
    void testFindAll_ErrorProducto_DebeLanzarExcepcion() {
        // Given
        when(ResenaRepository.findAll()).thenReturn(Arrays.asList(ResenaTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenThrow(mock(FeignException.class));

        // When & Then
        assertThatThrownBy(() -> ResenaService.findAll())
                .isInstanceOf(ResenaException.class)
                .hasMessageContaining("producto no existe");
    }
}
