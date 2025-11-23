package com.appmovil.msvc.pedidos.controller;

import com.appmovil.msvc.pedidos.clients.PagoClientRest;
import com.appmovil.msvc.pedidos.clients.ProductoClientRest;
import com.appmovil.msvc.pedidos.clients.UsuarioClientRest;
import com.appmovil.msvc.pedidos.dtos.PedidoDTO;
import com.appmovil.msvc.pedidos.dtos.PedidoCreationDTO;
import com.appmovil.msvc.pedidos.dtos.PedidoDetalleDTO;
import com.appmovil.msvc.pedidos.exceptions.PedidoException;
import com.appmovil.msvc.pedidos.model.Pago;
import com.appmovil.msvc.pedidos.model.Producto;
import com.appmovil.msvc.pedidos.model.Usuario;
import com.appmovil.msvc.pedidos.model.entity.Pedido;
import com.appmovil.msvc.pedidos.model.entity.PedidoDetalle;
import com.appmovil.msvc.pedidos.repositories.PedidoRepository;
import com.appmovil.msvc.pedidos.services.PedidoService;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PedidoServiceImpl implements PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioClientRest usuarioClientRest;

    @Autowired
    private ProductoClientRest productoClientRest;

    @Autowired
    private PagoClientRest pagoClientRest;

    // =======================================================================
    // LÓGICA PRINCIPAL: CHECKOUT/CREATE PEDIDO
    // =======================================================================

    @Override
    @Transactional
    public Pedido createPedido(PedidoCreationDTO creationDTO) {
        // 1. Validación de Usuario
        Usuario usuario;
        try {
            usuario = usuarioClientRest.findById(creationDTO.getIdUsuario());
            if (usuario == null) throw new PedidoException("Usuario no encontrado.");
        } catch (FeignException e) {
            throw new PedidoException("Error de comunicación con msvc-usuarios: " + e.getMessage());
        }

        // 2. Creación de Pedido y Lógica de Stock
        Pedido nuevoPedido = new Pedido();
        nuevoPedido.setIdUsuario(creationDTO.getIdUsuario());

        List<PedidoDetalle> detalles = new ArrayList<>();

        for (PedidoDetalleDTO itemDTO : creationDTO.getDetalles()) {
            Producto productoData;
            try {
                productoData = productoClientRest.findById(itemDTO.getIdProducto());
            } catch (FeignException e) {
                throw new PedidoException("Producto ID " + itemDTO.getIdProducto() + " no encontrado o msvc-productos no disponible.");
            }

            if (productoData.getStock() < itemDTO.getCantidad()) {
                throw new PedidoException("Stock insuficiente para el producto: " + productoData.getNombre());
            }

            PedidoDetalle detalle = new PedidoDetalle();
            detalle.setIdProducto(productoData.getId());
            detalle.setNombreProducto(productoData.getNombre());
            detalle.setPrecioUnitario(productoData.getPrecio());
            detalle.setCantidad(itemDTO.getCantidad());

            nuevoPedido.agregarDetalle(detalle);
        }


        Pago pagoRequest = new Pago();
        pagoRequest.setIdUsuario(creationDTO.getIdUsuario());
        pagoRequest.setMontoTotal(nuevoPedido.getTotalFinal() != null ? nuevoPedido.getTotalFinal() : 0);
        pagoRequest.setMetodoPago(creationDTO.getMetodoPago() != null ? creationDTO.getMetodoPago() : "Tarjeta");

        Pago pagoResultado;
        try {
            pagoResultado = pagoClientRest.registrarPago(pagoRequest);
        } catch (FeignException e) {
            throw new PedidoException("Fallo en la comunicación con msvc-pagos: " + e.getMessage());
        }

        if ("accepted".equalsIgnoreCase(pagoResultado.getEstadoTransaccion())) {
            nuevoPedido.setEstado("COMPLETADO");
            nuevoPedido.setFechaCompra(LocalDateTime.now());

        } else {
            nuevoPedido.setEstado("RECHAZADO");
        }

        return pedidoRepository.save(nuevoPedido);
    }


    private PedidoDTO convertToDTO(Pedido pedido) {
        PedidoDTO dto = new PedidoDTO();
        dto.setIdPedido(pedido.getId());
        dto.setEstado(pedido.getEstado());
        dto.setFechaCompra(pedido.getFechaCompra());
        dto.setSubtotal(pedido.getSubtotal());
        dto.setCostoEnvio(pedido.getCostoEnvio());
        dto.setTotalFinal(pedido.getTotalFinal());

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> findAll() {
        return pedidoRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Pedido findById(Long id) {
        return pedidoRepository.findById(id).orElseThrow(
                () -> new PedidoException("El Pedido con ID " + id + " no se encuentra.")
        );
    }

    @Override
    @Transactional
    public Pedido save(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new PedidoException("El Pedido con ID " + id + " no existe.");
        }
        pedidoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> findByIdUsuario(Long idUsuario) {
        return pedidoRepository.findByIdUsuario(idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> findByProductoId(Long idProducto) {
        // Implementación ineficiente por Stream, pero funcional para la prueba
        return pedidoRepository.findAll().stream()
                .filter(pedido -> pedido.getDetalles().stream()
                        .anyMatch(detalle -> detalle.getIdProducto().equals(idProducto)))
                .collect(Collectors.toList());
    }
}