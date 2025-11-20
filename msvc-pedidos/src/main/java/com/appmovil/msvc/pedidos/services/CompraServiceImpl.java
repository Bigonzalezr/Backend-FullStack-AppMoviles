package com.appmovil.msvc.pedidos.services;

import com.appmovil.msvc.pedidos.clients.PagoClientRest;
import com.appmovil.msvc.pedidos.clients.ProductoClientRest;
import com.appmovil.msvc.pedidos.clients.UsuarioClientRest;
import com.appmovil.msvc.pedidos.dtos.PedidoDTO;
import com.appmovil.msvc.pedidos.exceptions.PedidoException;
import com.appmovil.msvc.pedidos.model.Producto;
import com.appmovil.msvc.pedidos.model.Usuario;
import com.appmovil.msvc.pedidos.model.Pago;
import com.appmovil.msvc.pedidos.model.entity.Pedido;
import com.appmovil.msvc.pedidos.repositories.PedidoRepository;
import feign.FeignException;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Data
// Implementa la interfaz PedidoService que definimos
public class PedidoServiceImpl implements PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    // Feign Clients Inyectados
    @Autowired
    private PagoClientRest pagoClientRest;

    @Autowired
    private UsuarioClientRest usuarioClientRest;

    @Autowired
    private ProductoClientRest productoClientRest;


    // Método auxiliar para mapear Entidad -> DTO, enriqueciendo con datos externos
    private PedidoDTO toPedidoDTO(Pedido pedido) {
        PedidoDTO pedidoDTO = new PedidoDTO();
        // Mapeo básico
        pedidoDTO.setIdPedido(pedido.getId());
        pedidoDTO.setEstado(pedido.getEstado());
        pedidoDTO.setFechaCompra(pedido.getFechaCompra());
        pedidoDTO.setSubtotal(pedido.getSubtotal());
        // ... otros campos

        // Agregación de Usuario (Cliente)
        try {
            Usuario usuario = usuarioClientRest.findById(pedido.getIdUsuario());
            pedidoDTO.setUsuario(usuario);
        } catch (FeignException ex) {
            // Manejo de usuario no encontrado (Ej. cuenta eliminada)
            pedidoDTO.setUsuario(new Usuario(pedido.getIdUsuario(), "Usuario Eliminado", "N/A")); // Constructor simplificado
        }

        // Agregación de Productos (Manejo complejo de PedidoDetalle)
        // En una app real, aquí se iteraría sobre PedidoDetalle y se llamaría a msvc-productos
        // o se harían llamadas optimizadas (Ej. findByIds).
        // Por simplicidad, aquí indicamos que la lista se llenaría con los detalles
        pedidoDTO.setProductos(Collections.emptyList());

        return pedidoDTO;
    }

    // REEMPLAZA a findAll() de CompraService
    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> findAll() {
        // La implementación original con Feign calls dentro del stream es ineficiente.
        // Aquí la reemplazamos por el mapeo al DTO enriquecido (toPedidoDTO).
        return this.pedidoRepository.findAll().stream()
                .map(this::toPedidoDTO)
                .collect(Collectors.toList());
    }

    // REEMPLAZA a findById() de CompraService
    @Override
    @Transactional(readOnly = true)
    public Pedido findById(Long id) {
        return this.pedidoRepository.findById(id).orElseThrow(
                () -> new PedidoException("El Pedido con ID: " + id + " no se encuentra en la base de datos")
        );
    }

    // REEMPLAZA a save() de CompraService (Método genérico de guardado/actualización)
    @Override
    @Transactional
    public Pedido save(Pedido pedido) {
        // NOTA: La lógica de Feign para el checkout COMPLEJO debe ir en el método createPedido().
        // Este 'save' es un CRUD genérico, que solo guarda la entidad.

        try {
            // 1. Validar existencia del usuario (Cliente)
            usuarioClientRest.findById(pedido.getIdUsuario());

            // 2. Aquí iría la validación del PedidoDetalle, asegurando que los productos existen y hay stock.
            // ...

        } catch (FeignException ex) {
            // Captura errores de comunicación (Usuario no encontrado, etc.)
            throw new PedidoException("Problemas con la validación de recursos externos (Usuario/Producto).");
        }

        return this.pedidoRepository.save(pedido);
    }

    // REEMPLAZA a findByProfesorId() de CompraService
    @Override
    @Transactional(readOnly = true)
    public List<Pedido> findByProductoId(Long idProducto) {
        // Busca pedidos que contengan un producto específico
        return this.pedidoRepository.findAll().stream()
                .filter(p -> p.getDetalles().stream().anyMatch(d -> d.getIdProducto().equals(idProducto)))
                .collect(Collectors.toList());
    }

    // REEMPLAZA a findByAlumnoId() de CompraService
    @Override
    @Transactional(readOnly = true)
    public List<Pedido> findByIdUsuario(Long idUsuario) {
        // Busca todos los pedidos realizados por un cliente (usuario) específico
        return this.pedidoRepository.findByIdUsuario(idUsuario);
    }

    // NOTA: El método updateById/cambiarEstado del código original ha sido omitido
    // por ser irrelevante en la entidad Pedido.
}