package com.appmovil.msvc.pedidos.services;


import com.appmovil.msvc.pedidos.controller.PedidoServiceImpl;
import com.appmovil.msvc.pedidos.dtos.PedidoDTO;
import com.appmovil.msvc.pedidos.exceptions.PedidoException;
import com.appmovil.msvc.pedidos.model.entity.Pedido;
import com.appmovil.msvc.pedidos.model.entity.PedidoDetalle;
import com.appmovil.msvc.pedidos.repositories.PedidoRepository;
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
public class PedidoServiceTest { // Nombre de clase corregido

    @Mock
    private PedidoRepository pedidoRepository; // Repositorio corregido

    @InjectMocks
    private PedidoServiceImpl pedidoService; // Servicio implementado corregido

    private Pedido pedidoPrueba;
    private List<Pedido> pedidos = new ArrayList<>();
    private Faker faker;

    // --- Métodos Auxiliares para Setup ---

    private PedidoDetalle createDetalle(Long idProducto, int cantidad, int precio) {
        PedidoDetalle detalle = new PedidoDetalle();
        detalle.setIdProducto(idProducto);
        detalle.setNombreProducto(faker.commerce().productName());
        detalle.setPrecioUnitario(precio);
        detalle.setCantidad(cantidad);
        return detalle;
    }

    private Pedido createPedidoWithDetails(Long id, Long idUsuario) {
        Pedido p = new Pedido();
        p.setId(id);
        p.setIdUsuario(idUsuario);
        p.setFechaCompra(LocalDateTime.now());

        // Agregamos dos ítems de detalle (simulando una compra)
        p.agregarDetalle(createDetalle(101L, 2, 50000)); // Total 100,000
        p.agregarDetalle(createDetalle(102L, 1, 120000)); // Total 120,000

        // Nota: @PrePersist/@PreUpdate calculará los totales al guardar, pero para el mock es suficiente.
        p.setSubtotal(220000);
        p.setCostoEnvio(0);
        p.setTotalFinal(220000);
        p.setEstado("COMPLETADO");
        return p;
    }

    // --- Configuración Inicial ---

    @BeforeEach
    public void setUp() {
        this.faker = new Faker(new Locale("es", "CL"));

        // 1. Pedido de prueba (ID 1, Total 220,000, 0 envío)
        this.pedidoPrueba = createPedidoWithDetails(1L, 222L);

        // 2. Generación de 100 pedidos aleatorios para la lista
        for (int i = 0; i < 100; i++) {
            Pedido p = createPedidoWithDetails((long) i + 2, (long) faker.number().numberBetween(1, 100));
            pedidos.add(p);
        }
    }

    // --- Tests Funcionales ---

    @Test
    @DisplayName("Devolver todos los pedidos")
    public void shouldFindAllPedidos() { // Nombre del método corregido
        pedidos.add(pedidoPrueba);
        when(pedidoRepository.findAll()).thenReturn(pedidos);

        // El servicio devuelve una lista de PedidoDTOs (findAll())
        List<PedidoDTO> result = pedidoService.findAll();

        assertThat(result).hasSize(101);
        // Verificar que el repositorio fue llamado correctamente
        verify(pedidoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Encontrar pedido por ID")
    public void shouldFindPedidoById() { // Nombre del método corregido
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoPrueba));
        Pedido result = pedidoService.findById(1L);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(pedidoPrueba);
        verify(pedidoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Lanzar excepción al no encontrar Pedido por ID")
    public void shouldNotFindPedidoById() { // Nombre del método corregido
        Long idInexistente = 9999L;
        when(pedidoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.findById(idInexistente))
                .isInstanceOf(PedidoException.class) // Excepción corregida
                .hasMessageContaining("El Pedido con ID: " + idInexistente + " no se encuentra"); // Mensaje corregido

        verify(pedidoRepository, times(1)).findById(idInexistente);
    }

    @Test
    @DisplayName("Debería guardar un pedido")
    public void shouldSavePedido() { // Nombre del método corregido
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoPrueba);
        Pedido result = pedidoService.save(pedidoPrueba);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(pedidoPrueba);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }
}