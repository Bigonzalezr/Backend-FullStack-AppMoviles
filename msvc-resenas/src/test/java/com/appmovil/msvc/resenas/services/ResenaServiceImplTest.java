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
    private ResenaRepository resenaRepository;

    @Mock
    private UsuarioClientRest usuarioClientRest;

    @Mock
    private ProductoClientRest productoClientRest;

    @InjectMocks
    private ResenaServiceImpl resenaService;

    private Resena resenaTest;
    private Usuario usuarioTest;
    private Producto productoTest;

    @BeforeEach
    void setUp() {
        usuarioTest = new Usuario();
        usuarioTest.setId(1L);
        usuarioTest.setNombre("Juan Pérez");
        usuarioTest.setActivo(true);

        productoTest = new Producto();
        productoTest.setId(1L);
        productoTest.setNombre("iPhone 15");
        productoTest.setActivo(true);

        resenaTest = new Resena();
        resenaTest.setId(1L);
        resenaTest.setIdUsuario(1L);
        resenaTest.setIdProducto(1L);
        resenaTest.setRating(5);
        resenaTest.setComentario("Excelente producto, muy recomendado");
        resenaTest.setFechaCreacion(LocalDateTime.now());
        resenaTest.setActivo(true);
    }

    @Test
    @DisplayName("findAll - Debe retornar lista de Resenas enriquecidas")
    void testFindAll_DebeRetornarListaEnriquecida() {
        // Given
        when(resenaRepository.findAll()).thenReturn(Arrays.asList(resenaTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When
        List<ResenaDTO> resultado = resenaService.findAll();

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombreUsuario()).isEqualTo("Juan Pérez");
        assertThat(resultado.get(0).getNombreProducto()).isEqualTo("iPhone 15");
        assertThat(resultado.get(0).getRating()).isEqualTo(5);
        verify(resenaRepository).findAll();
        verify(usuarioClientRest).findById(1L);
        verify(productoClientRest).findById(1L);
    }

    @Test
    @DisplayName("findById - Debe retornar Resena cuando existe")
    void testFindById_ConIdExistente_DebeRetornarResena() {
        // Given
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resenaTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When
        ResenaDTO resultado = resenaService.findById(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getComentario()).isEqualTo("Excelente producto, muy recomendado");
        verify(resenaRepository).findById(1L);
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando Resena no existe")
    void testFindById_ConIdInexistente_DebeLanzarExcepcion() {
        // Given
        when(resenaRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resenaService.findById(999L))
                .isInstanceOf(ResenaException.class)
                .hasMessageContaining("999");
        
        verify(resenaRepository).findById(999L);
    }

    @Test
    @DisplayName("save - Debe guardar Resena exitosamente")
    void testSave_ConDatosValidos_DebeGuardarExitosamente() {
        // Given
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);
        when(resenaRepository.existsByIdUsuarioAndIdProducto(1L, 1L)).thenReturn(false);
        when(resenaRepository.save(any(Resena.class))).thenAnswer(inv -> {
            Resena r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // When
        ResenaDTO resultado = resenaService.save(resenaTest);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getRating()).isEqualTo(5);
        verify(usuarioClientRest).findById(1L);
        verify(productoClientRest).findById(1L);
        verify(resenaRepository).existsByIdUsuarioAndIdProducto(1L, 1L);
        verify(resenaRepository).save(any(Resena.class));
    }

    @Test
    @DisplayName("save - Debe lanzar excepción cuando usuario no existe")
    void testSave_ConUsuarioInexistente_DebeLanzarExcepcion() {
        // Given
        when(usuarioClientRest.findById(1L)).thenThrow(mock(FeignException.class));

        // When & Then
        assertThatThrownBy(() -> resenaService.save(resenaTest))
                .isInstanceOf(ResenaException.class)
                .hasMessageContaining("Usuario no existe");
        
        verify(resenaRepository, never()).save(any());
    }

    @Test
    @DisplayName("save - Debe lanzar excepción cuando producto no existe")
    void testSave_ConProductoInexistente_DebeLanzarExcepcion() {
        // Given
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenThrow(mock(FeignException.class));

        // When & Then
        assertThatThrownBy(() -> resenaService.save(resenaTest))
                .isInstanceOf(ResenaException.class)
                .hasMessageContaining("Producto no existe");
        
        verify(resenaRepository, never()).save(any());
    }

    @Test
    @DisplayName("save - Debe lanzar excepción cuando ya existe Resena del usuario para el producto")
    void testSave_ConResenaDuplicada_DebeLanzarExcepcion() {
        // Given
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);
        when(resenaRepository.existsByIdUsuarioAndIdProducto(1L, 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> resenaService.save(resenaTest))
                .isInstanceOf(ResenaException.class)
                .hasMessageContaining("Ya existe una Resena");
        
        verify(resenaRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe actualizar Resena exitosamente")
    void testUpdate_ConDatosValidos_DebeActualizarExitosamente() {
        // Given
        Resena ResenaActualizada = new Resena();
        ResenaActualizada.setRating(4);
        ResenaActualizada.setComentario("Actualizado: Buen producto pero mejorable");

        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resenaTest));
        when(resenaRepository.save(any(Resena.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When
        ResenaDTO resultado = resenaService.update(1L, ResenaActualizada);

        // Then
        assertThat(resultado.getRating()).isEqualTo(4);
        assertThat(resultado.getComentario()).contains("Actualizado");
        verify(resenaRepository).save(argThat(r -> 
            r.getRating() == 4 && r.getComentario().contains("Actualizado")
        ));
    }

    @Test
    @DisplayName("update - Debe lanzar excepción si Resena no existe")
    void testUpdate_ConIdInexistente_DebeLanzarExcepcion() {
        // Given
        when(resenaRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resenaService.update(999L, resenaTest))
                .isInstanceOf(ResenaException.class)
                .hasMessageContaining("999");
        
        verify(resenaRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete - Debe desactivar Resena (soft delete)")
    void testDelete_DebeDesactivarResena() {
        // Given
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resenaTest));
        when(resenaRepository.save(any(Resena.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        resenaService.delete(1L);

        // Then
        verify(resenaRepository).save(argThat(r -> !r.getActivo()));
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción si Resena no existe")
    void testDelete_ConIdInexistente_DebeLanzarExcepcion() {
        // Given
        when(resenaRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resenaService.delete(999L))
                .isInstanceOf(ResenaException.class);
        
        verify(resenaRepository, never()).save(any());
    }

    @Test
    @DisplayName("findByUsuario - Debe retornar Resenas del usuario")
    void testFindByUsuario_DebeRetornarResenasDelUsuario() {
        // Given
        when(resenaRepository.findByIdUsuario(1L)).thenReturn(Arrays.asList(resenaTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When
        List<ResenaDTO> resultado = resenaService.findByUsuario(1L);

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdUsuario()).isEqualTo(1L);
        verify(resenaRepository).findByIdUsuario(1L);
    }

    @Test
    @DisplayName("findByProducto - Debe retornar solo Resenas activas del producto")
    void testFindByProducto_DebeRetornarSoloResenasActivas() {
        // Given
        when(resenaRepository.findByIdProductoAndActivo(1L, true)).thenReturn(Arrays.asList(resenaTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When
        List<ResenaDTO> resultado = resenaService.findByProducto(1L);

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getActivo()).isTrue();
        assertThat(resultado.get(0).getIdProducto()).isEqualTo(1L);
        verify(resenaRepository).findByIdProductoAndActivo(1L, true);
    }

    @Test
    @DisplayName("getAverageRatingByProducto - Debe calcular promedio correcto")
    void testGetAverageRatingByProducto_DebeCalcularPromedio() {
        // Given
        when(resenaRepository.findAverageRatingByProducto(1L)).thenReturn(4.5);

        // When
        Double promedio = resenaService.getAverageRatingByProducto(1L);

        // Then
        assertThat(promedio).isEqualTo(4.5);
        verify(resenaRepository).findAverageRatingByProducto(1L);
    }

    @Test
    @DisplayName("getAverageRatingByProducto - Debe retornar 0.0 si no hay Resenas")
    void testGetAverageRatingByProducto_SinResenas_DebeRetornar0() {
        // Given
        when(resenaRepository.findAverageRatingByProducto(1L)).thenReturn(null);

        // When
        Double promedio = resenaService.getAverageRatingByProducto(1L);

        // Then
        assertThat(promedio).isEqualTo(0.0);
    }

    @Test
    @DisplayName("findAll - Debe lanzar excepción si falla obtener usuario")
    void testFindAll_ErrorUsuario_DebeLanzarExcepcion() {
        // Given
        when(resenaRepository.findAll()).thenReturn(Arrays.asList(resenaTest));
        when(usuarioClientRest.findById(1L)).thenThrow(mock(FeignException.class));

        // When & Then
        assertThatThrownBy(() -> resenaService.findAll())
                .isInstanceOf(ResenaException.class)
                .hasMessageContaining("usuario no existe");
    }

    @Test
    @DisplayName("findAll - Debe lanzar excepción si falla obtener producto")
    void testFindAll_ErrorProducto_DebeLanzarExcepcion() {
        // Given
        when(resenaRepository.findAll()).thenReturn(Arrays.asList(resenaTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenThrow(mock(FeignException.class));

        // When & Then
        assertThatThrownBy(() -> resenaService.findAll())
                .isInstanceOf(ResenaException.class)
                .hasMessageContaining("producto no existe");
    }
}
