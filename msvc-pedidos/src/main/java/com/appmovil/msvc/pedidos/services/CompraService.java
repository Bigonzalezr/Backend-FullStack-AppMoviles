package com.appmovil.msvc.pedidos.services;

import com.appmovil.msvc.pedidos.dtos.PedidoDTO;
import com.appmovil.msvc.pedidos.model.entity.Pedido;

import java.util.List;

interface pedidoService {

    List<PedidoDTO> findAll();

    Pedido findById(Long id);

    Pedido save(Pedido pedido);

    List<Pedido> findByIdUsuario(Long idUsuario);

    List<Pedido> findByProductoId(Long idProducto);
}
