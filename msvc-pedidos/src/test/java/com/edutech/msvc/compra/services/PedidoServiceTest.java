package com.edutech.msvc.compra.services;

import com.edutech.msvc.compra.exceptions.CompraException;
import com.edutech.msvc.boleta.models.entities.Compra;
import com.edutech.msvc.compra.repositories.CompraRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock
    private CompraRepository compraRepository;

    @InjectMocks
    private CompraServiceImpl compraService;

    private Compra compraPrueba;
    private List<Compra> compras = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        this.compraPrueba = new Compra();
        compraPrueba.setIdCompra(1L);
        compraPrueba.setHoraCompra(LocalDateTime.now());
        compraPrueba.setTotal(15000);
        compraPrueba.setIdCurso(111L);
        compraPrueba.setIdAlumno(222L);

        Faker faker = new Faker(new Locale("es", "CL"));
        for (int i = 0; i < 100; i++) {
            Compra c = new Compra();
            c.setIdCompra((long) i + 2);
            c.setHoraCompra(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 30)));
            c.setTotal(faker.number().numberBetween(5000, 30000));
            c.setIdCurso((long) faker.number().numberBetween(100, 200));
            c.setIdAlumno((long) faker.number().numberBetween(200, 300));
            compras.add(c);
        }
    }

    @Test
    @DisplayName("Devuelve todas las compras")
    public void shouldFindAllCompras() {
        compras.add(compraPrueba);
        when(compraRepository.findAll()).thenReturn(compras);

        List<Compra> result = compraService.findAll();

        assertThat(result).hasSize(101);
        assertThat(result).contains(compraPrueba);
        verify(compraRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Encontrar por ID una compra")
    public void shouldFindCompraById() {
        when(compraRepository.findById(1L)).thenReturn(Optional.of(compraPrueba));
        Compra result = compraService.findById(1L);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(compraPrueba);
        verify(compraRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Pedido no encontrada por ID")
    public void shouldNotFindCompraById() {
        Long idInexistente = 9999L;
        when(compraRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> compraService.findById(idInexistente))
                .isInstanceOf(CompraException.class)
                .hasMessageContaining("La compra con id: " + idInexistente + " no se encuentra en la base de datos");

        verify(compraRepository, times(1)).findById(idInexistente);
    }

    @Test
    @DisplayName("Debería guardar una compra")
    public void shouldSaveCompra() {
        when(compraRepository.save(any(Compra.class))).thenReturn(compraPrueba);
        Compra result = compraService.save(compraPrueba);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(compraPrueba);
        verify(compraRepository, times(1)).save(any(Compra.class));
    }
}
