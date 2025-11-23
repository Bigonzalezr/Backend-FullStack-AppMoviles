package com.appmovil.msvc.pedidos.services;

import com.appmovil.msvc.pedidos.dtos.PedidoCreationDTO;
import com.appmovil.msvc.pedidos.dtos.PedidoDTO;
import com.appmovil.msvc.pedidos.model.entity.Pedido;
import java.util.List;

public interface PedidoService {

    Pedido createPedido(PedidoCreationDTO creationDTO);

    Pedido save(Pedido pedido);

    void delete(Long id);

    List<PedidoDTO> findAll();

    Pedido findById(Long id);

    List<Pedido> findByIdUsuario(Long idUsuario);

    List<Pedido> findByProductoId(Long idProducto);
}