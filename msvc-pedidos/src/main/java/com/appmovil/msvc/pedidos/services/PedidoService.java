package com.appmovil.msvc.pedidos.services;

import com.appmovil.msvc.pedidos.dtos.PedidoCreationDTO;
import com.appmovil.msvc.pedidos.dtos.PedidoDTO;
import java.util.List;

public interface PedidoService {

    PedidoDTO crearPedido(PedidoCreationDTO creationDTO);

    PedidoDTO actualizarEstado(Long idPedido, String nuevoEstado);

    void actualizarEstadoPago(Long idPedido);

    PedidoDTO findById(Long id);

    List<PedidoDTO> findAll();

    List<PedidoDTO> findByUsuario(Long idUsuario);

    List<PedidoDTO> findByEstado(String estado);

    void cancelarPedido(Long idPedido);
}