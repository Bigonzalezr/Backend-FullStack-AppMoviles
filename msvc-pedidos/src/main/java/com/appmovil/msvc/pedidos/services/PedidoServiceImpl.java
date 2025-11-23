package com.appmovil.msvc.pedidos.services;

import com.appmovil.msvc.pedidos.clients.PagoClientRest;
import com.appmovil.msvc.pedidos.clients.ProductoClientRest;
import com.appmovil.msvc.pedidos.clients.UsuarioClientRest;
import com.appmovil.msvc.pedidos.dtos.PedidoDTO;
import com.appmovil.msvc.pedidos.dtos.PedidoCreationDTO;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Data
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


    private PedidoDTO toPedidoDTO(Pedido pedido) {
        PedidoDTO pedidoDTO = new PedidoDTO();

        pedidoDTO.setIdPedido(pedido.getId());
        pedidoDTO.setEstado(pedido.getEstado());
        pedidoDTO.setFechaCompra(pedido.getFechaCompra());
        pedidoDTO.setSubtotal(pedido.getSubtotal());
        pedidoDTO.setCostoEnvio(pedido.getCostoEnvio());
        pedidoDTO.setTotalFinal(pedido.getTotalFinal());

        try {
            Usuario usuario = usuarioClientRest.findById(pedido.getIdUsuario());
            pedidoDTO.setUsuario(Usuario);
        } catch (FeignException ex) {

            pedidoDTO.setUsuario(new Usuario(pedido.getIdUsuario(), "Usuario Desconocido", "N/A", "N/A", "N/A", false, null));
        }

        // 2. Agregación de Productos (Manejo complejo de PedidoDetalle)
        pedidoDTO.setProductos(Collections.emptyList());

        return pedidoDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> findAll() {
        return this.pedidoRepository.findAll().stream()
                .map(this::toPedidoDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Pedido findById(Long id) {
        return this.pedidoRepository.findById(id).orElseThrow(
                () -> new PedidoException("El Pedido con ID: " + id + " no se encuentra en la base de datos")
        );
    }

    @Override
    @Transactional
    public Pedido save(Pedido pedido) {
        try {

            usuarioClientRest.findById(pedido.getIdUsuario());
            if (!pedido.getDetalles().isEmpty()) {
                productoClientRest.findById(pedido.getDetalles().get(0).getIdProducto());
            }
        } catch (FeignException ex) {
            throw new PedidoException("Problemas con la validación de Usuario o Producto (código externo: " + ex.status() + ")");
        }

        return this.pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido createPedido(PedidoCreationDTO creationDTO) {
        return null;
    }


    @Override
    @Transactional(readOnly = true)
    public List<Pedido> findByProductoId(Long idProducto) {
        return this.pedidoRepository.findAll().stream()
                .filter(p -> p.getDetalles().stream().anyMatch(d -> d.getIdProducto().equals(idProducto)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> findByIdUsuario(Long idUsuario) {
        // Usa el método del Repositorio
        return this.pedidoRepository.findByIdUsuario(idUsuario);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new PedidoException("El Pedido con ID " + id + " no existe.");
        }
        pedidoRepository.deleteById(id);
    }
}