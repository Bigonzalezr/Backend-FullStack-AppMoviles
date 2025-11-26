package com.appmovil.msvc.resenas.services;

import com.appmovil.msvc.resenas.clients.ProductoClientRest;
import com.appmovil.msvc.resenas.clients.UsuarioClientRest;
import com.appmovil.msvc.resenas.dtos.ReseñaDTO;
import com.appmovil.msvc.resenas.exceptions.ReseñaException;
import com.appmovil.msvc.resenas.models.Producto;
import com.appmovil.msvc.resenas.models.Usuario;
import com.appmovil.msvc.resenas.models.entities.Reseña;
import com.appmovil.msvc.resenas.repositories.ReseñaRepository;
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
@DisplayName("ReseñaService - Unit Tests")
class ReseñaServiceImplTest {

    @Mock
    private ReseñaRepository reseñaRepository;

    @Mock
    private UsuarioClientRest usuarioClientRest;

    @Mock
    private ProductoClientRest productoClientRest;

    @InjectMocks
    private ReseñaServiceImpl reseñaService;

    private Reseña reseñaTest;
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

        reseñaTest = new Reseña();
        reseñaTest.setId(1L);
        reseñaTest.setIdUsuario(1L);
        reseñaTest.setIdProducto(1L);
        reseñaTest.setRating(5);
        reseñaTest.setComentario("Excelente producto, muy recomendado");
        reseñaTest.setFechaCreacion(LocalDateTime.now());
        reseñaTest.setActivo(true);
    }

    @Test
    @DisplayName("findAll - Debe retornar lista de reseñas enriquecidas")
    void testFindAll_DebeRetornarListaEnriquecida() {
        // Given
        when(reseñaRepository.findAll()).thenReturn(Arrays.asList(reseñaTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When
        List<ReseñaDTO> resultado = reseñaService.findAll();

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombreUsuario()).isEqualTo("Juan Pérez");
        assertThat(resultado.get(0).getNombreProducto()).isEqualTo("iPhone 15");
        assertThat(resultado.get(0).getRating()).isEqualTo(5);
        verify(reseñaRepository).findAll();
        verify(usuarioClientRest).findById(1L);
        verify(productoClientRest).findById(1L);
    }

    @Test
    @DisplayName("findById - Debe retornar reseña cuando existe")
    void testFindById_ConIdExistente_DebeRetornarReseña() {
        // Given
        when(reseñaRepository.findById(1L)).thenReturn(Optional.of(reseñaTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When
        ReseñaDTO resultado = reseñaService.findById(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getComentario()).isEqualTo("Excelente producto, muy recomendado");
        verify(reseñaRepository).findById(1L);
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando reseña no existe")
    void testFindById_ConIdInexistente_DebeLanzarExcepcion() {
        // Given
        when(reseñaRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reseñaService.findById(999L))
                .isInstanceOf(ReseñaException.class)
                .hasMessageContaining("999");
        
        verify(reseñaRepository).findById(999L);
    }

    @Test
    @DisplayName("save - Debe guardar reseña exitosamente")
    void testSave_ConDatosValidos_DebeGuardarExitosamente() {
        // Given
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);
        when(reseñaRepository.existsByIdUsuarioAndIdProducto(1L, 1L)).thenReturn(false);
        when(reseñaRepository.save(any(Reseña.class))).thenAnswer(inv -> {
            Reseña r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // When
        ReseñaDTO resultado = reseñaService.save(reseñaTest);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getRating()).isEqualTo(5);
        verify(usuarioClientRest).findById(1L);
        verify(productoClientRest).findById(1L);
        verify(reseñaRepository).existsByIdUsuarioAndIdProducto(1L, 1L);
        verify(reseñaRepository).save(any(Reseña.class));
    }

    @Test
    @DisplayName("save - Debe lanzar excepción cuando usuario no existe")
    void testSave_ConUsuarioInexistente_DebeLanzarExcepcion() {
        // Given
        when(usuarioClientRest.findById(1L)).thenThrow(mock(FeignException.class));

        // When & Then
        assertThatThrownBy(() -> reseñaService.save(reseñaTest))
                .isInstanceOf(ReseñaException.class)
                .hasMessageContaining("Usuario no existe");
        
        verify(reseñaRepository, never()).save(any());
    }

    @Test
    @DisplayName("save - Debe lanzar excepción cuando producto no existe")
    void testSave_ConProductoInexistente_DebeLanzarExcepcion() {
        // Given
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenThrow(mock(FeignException.class));

        // When & Then
        assertThatThrownBy(() -> reseñaService.save(reseñaTest))
                .isInstanceOf(ReseñaException.class)
                .hasMessageContaining("Producto no existe");
        
        verify(reseñaRepository, never()).save(any());
    }

    @Test
    @DisplayName("save - Debe lanzar excepción cuando ya existe reseña del usuario para el producto")
    void testSave_ConReseñaDuplicada_DebeLanzarExcepcion() {
        // Given
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);
        when(reseñaRepository.existsByIdUsuarioAndIdProducto(1L, 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> reseñaService.save(reseñaTest))
                .isInstanceOf(ReseñaException.class)
                .hasMessageContaining("Ya existe una reseña");
        
        verify(reseñaRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe actualizar reseña exitosamente")
    void testUpdate_ConDatosValidos_DebeActualizarExitosamente() {
        // Given
        Reseña reseñaActualizada = new Reseña();
        reseñaActualizada.setRating(4);
        reseñaActualizada.setComentario("Actualizado: Buen producto pero mejorable");

        when(reseñaRepository.findById(1L)).thenReturn(Optional.of(reseñaTest));
        when(reseñaRepository.save(any(Reseña.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When
        ReseñaDTO resultado = reseñaService.update(1L, reseñaActualizada);

        // Then
        assertThat(resultado.getRating()).isEqualTo(4);
        assertThat(resultado.getComentario()).contains("Actualizado");
        verify(reseñaRepository).save(argThat(r -> 
            r.getRating() == 4 && r.getComentario().contains("Actualizado")
        ));
    }

    @Test
    @DisplayName("update - Debe lanzar excepción si reseña no existe")
    void testUpdate_ConIdInexistente_DebeLanzarExcepcion() {
        // Given
        when(reseñaRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reseñaService.update(999L, reseñaTest))
                .isInstanceOf(ReseñaException.class)
                .hasMessageContaining("999");
        
        verify(reseñaRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete - Debe desactivar reseña (soft delete)")
    void testDelete_DebeDesactivarReseña() {
        // Given
        when(reseñaRepository.findById(1L)).thenReturn(Optional.of(reseñaTest));
        when(reseñaRepository.save(any(Reseña.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        reseñaService.delete(1L);

        // Then
        verify(reseñaRepository).save(argThat(r -> !r.getActivo()));
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción si reseña no existe")
    void testDelete_ConIdInexistente_DebeLanzarExcepcion() {
        // Given
        when(reseñaRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reseñaService.delete(999L))
                .isInstanceOf(ReseñaException.class);
        
        verify(reseñaRepository, never()).save(any());
    }

    @Test
    @DisplayName("findByUsuario - Debe retornar reseñas del usuario")
    void testFindByUsuario_DebeRetornarReseñasDelUsuario() {
        // Given
        when(reseñaRepository.findByIdUsuario(1L)).thenReturn(Arrays.asList(reseñaTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When
        List<ReseñaDTO> resultado = reseñaService.findByUsuario(1L);

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdUsuario()).isEqualTo(1L);
        verify(reseñaRepository).findByIdUsuario(1L);
    }

    @Test
    @DisplayName("findByProducto - Debe retornar solo reseñas activas del producto")
    void testFindByProducto_DebeRetornarSoloReseñasActivas() {
        // Given
        when(reseñaRepository.findByIdProductoAndActivo(1L, true)).thenReturn(Arrays.asList(reseñaTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenReturn(productoTest);

        // When
        List<ReseñaDTO> resultado = reseñaService.findByProducto(1L);

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getActivo()).isTrue();
        assertThat(resultado.get(0).getIdProducto()).isEqualTo(1L);
        verify(reseñaRepository).findByIdProductoAndActivo(1L, true);
    }

    @Test
    @DisplayName("getAverageRatingByProducto - Debe calcular promedio correcto")
    void testGetAverageRatingByProducto_DebeCalcularPromedio() {
        // Given
        when(reseñaRepository.findAverageRatingByProducto(1L)).thenReturn(4.5);

        // When
        Double promedio = reseñaService.getAverageRatingByProducto(1L);

        // Then
        assertThat(promedio).isEqualTo(4.5);
        verify(reseñaRepository).findAverageRatingByProducto(1L);
    }

    @Test
    @DisplayName("getAverageRatingByProducto - Debe retornar 0.0 si no hay reseñas")
    void testGetAverageRatingByProducto_SinReseñas_DebeRetornar0() {
        // Given
        when(reseñaRepository.findAverageRatingByProducto(1L)).thenReturn(null);

        // When
        Double promedio = reseñaService.getAverageRatingByProducto(1L);

        // Then
        assertThat(promedio).isEqualTo(0.0);
    }

    @Test
    @DisplayName("findAll - Debe lanzar excepción si falla obtener usuario")
    void testFindAll_ErrorUsuario_DebeLanzarExcepcion() {
        // Given
        when(reseñaRepository.findAll()).thenReturn(Arrays.asList(reseñaTest));
        when(usuarioClientRest.findById(1L)).thenThrow(mock(FeignException.class));

        // When & Then
        assertThatThrownBy(() -> reseñaService.findAll())
                .isInstanceOf(ReseñaException.class)
                .hasMessageContaining("usuario no existe");
    }

    @Test
    @DisplayName("findAll - Debe lanzar excepción si falla obtener producto")
    void testFindAll_ErrorProducto_DebeLanzarExcepcion() {
        // Given
        when(reseñaRepository.findAll()).thenReturn(Arrays.asList(reseñaTest));
        when(usuarioClientRest.findById(1L)).thenReturn(usuarioTest);
        when(productoClientRest.findById(1L)).thenThrow(mock(FeignException.class));

        // When & Then
        assertThatThrownBy(() -> reseñaService.findAll())
                .isInstanceOf(ReseñaException.class)
                .hasMessageContaining("producto no existe");
    }
}
