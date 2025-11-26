package com.appmovil.msvc.pedidos.services;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PedidoServiceImpl implements PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioClientRest usuarioClientRest;

    @Autowired
    private ProductoClientRest productoClientRest;

    @Override
    @Transactional
    public PedidoDTO crearPedido(PedidoCreationDTO creationDTO) {
        log.info("Creando nuevo pedido para usuario: {}", creationDTO.getIdUsuario());

        // Validar usuario
        Usuario usuario;
        try {
            usuario = usuarioClientRest.findById(creationDTO.getIdUsuario());
            if (usuario == null || !usuario.getActivo()) {
                throw new PedidoException("El usuario no existe o está inactivo");
            }
        } catch (FeignException ex) {
            throw new PedidoException("Error al validar el usuario: " + ex.getMessage());
        }

        // Crear el pedido
        Pedido pedido = new Pedido();
        pedido.setIdUsuario(creationDTO.getIdUsuario());
        pedido.setEstado("PENDIENTE");
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setDireccionEnvio(creationDTO.getDireccionEnvio());
        pedido.setMetodoPago(creationDTO.getMetodoPago());
        pedido.setNotas(creationDTO.getNotas());

        // Agregar detalles y validar productos
        for (PedidoDetalleDTO detalleDTO : creationDTO.getDetalles()) {
            Producto producto;
            try {
                producto = productoClientRest.findById(detalleDTO.getIdProducto());
                if (producto == null || !producto.getActivo()) {
                    throw new PedidoException("El producto " + detalleDTO.getIdProducto() + " no existe o no está activo");
                }
                if (producto.getStock() < detalleDTO.getCantidad()) {
                    throw new PedidoException("Stock insuficiente para el producto: " + producto.getNombre());
                }
            } catch (FeignException ex) {
                throw new PedidoException("Error al validar el producto: " + ex.getMessage());
            }

            PedidoDetalle detalle = new PedidoDetalle();
            detalle.setIdProducto(producto.getIdProducto());
            detalle.setNombreProducto(producto.getNombre());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setCantidad(detalleDTO.getCantidad());
            pedido.agregarDetalle(detalle);

            // Reducir stock
            try {
                productoClientRest.updateStock(producto.getIdProducto(), -detalleDTO.getCantidad());
            } catch (Exception ex) {
                log.warn("No se pudo actualizar el stock del producto: {}", ex.getMessage());
            }
        }

        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        return convertirADTO(pedidoGuardado);
    }

    @Override
    @Transactional
    public PedidoDTO actualizarEstado(Long idPedido, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + idPedido));

        pedido.setEstado(nuevoEstado);
        Pedido pedidoActualizado = pedidoRepository.save(pedido);
        return convertirADTO(pedidoActualizado);
    }

    @Override
    @Transactional
    public void actualizarEstadoPago(Long idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + idPedido));

        pedido.setEstado("PAGADO");
        pedidoRepository.save(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoDTO findById(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));
        return convertirADTO(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> findAll() {
        return pedidoRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> findByUsuario(Long idUsuario) {
        return pedidoRepository.findByIdUsuarioOrderByFechaPedidoDesc(idUsuario).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> findByEstado(String estado) {
        return pedidoRepository.findByEstado(estado).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelarPedido(Long idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + idPedido));

        if (!"PENDIENTE".equals(pedido.getEstado())) {
            throw new PedidoException("Solo se pueden cancelar pedidos en estado PENDIENTE");
        }

        // Restaurar stock
        for (PedidoDetalle detalle : pedido.getDetalles()) {
            try {
                productoClientRest.updateStock(detalle.getIdProducto(), detalle.getCantidad());
            } catch (Exception ex) {
                log.warn("No se pudo restaurar el stock del producto: {}", ex.getMessage());
            }
        }

        pedido.setEstado("CANCELADO");
        pedidoRepository.save(pedido);
    }

    private PedidoDTO convertirADTO(Pedido pedido) {
        PedidoDTO dto = new PedidoDTO();
        dto.setIdPedido(pedido.getId());
        dto.setIdUsuario(pedido.getIdUsuario());
        dto.setEstado(pedido.getEstado());
        dto.setFechaPedido(pedido.getFechaPedido());
        dto.setDireccionEnvio(pedido.getDireccionEnvio());
        dto.setMetodoPago(pedido.getMetodoPago());
        dto.setNotas(pedido.getNotas());
        dto.setSubtotal(pedido.getSubtotal());
        dto.setCostoEnvio(pedido.getCostoEnvio());
        dto.setTotal(pedido.getTotal());

        // Enriquecer con datos del usuario
        try {
            Usuario usuario = usuarioClientRest.findById(pedido.getIdUsuario());
            if (usuario != null) {
                dto.setNombreUsuario(usuario.getNombre() + " " + usuario.getApellido());
                dto.setEmailUsuario(usuario.getEmail());
            }
        } catch (Exception ex) {
            log.warn("No se pudo obtener información del usuario: {}", ex.getMessage());
        }

        // Convertir detalles
        List<PedidoDetalleDTO> detallesDTO = pedido.getDetalles().stream()
                .map(detalle -> {
                    PedidoDetalleDTO detalleDTO = new PedidoDetalleDTO();
                    detalleDTO.setIdDetalle(detalle.getId());
                    detalleDTO.setIdProducto(detalle.getIdProducto());
                    detalleDTO.setNombreProducto(detalle.getNombreProducto());
                    detalleDTO.setPrecioUnitario(detalle.getPrecioUnitario());
                    detalleDTO.setCantidad(detalle.getCantidad());
                    detalleDTO.setSubtotal(detalle.getTotalLinea());
                    return detalleDTO;
                })
                .collect(Collectors.toList());
        dto.setDetalles(detallesDTO);

        return dto;
    }
}
