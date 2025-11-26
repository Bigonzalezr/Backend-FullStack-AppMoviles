package com.appmovil.msvc.pedidos.repositories;

import com.appmovil.msvc.pedidos.model.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByIdUsuario(Long idUsuario);

    List<Pedido> findByIdUsuarioAndEstado(Long idUsuario, String estado);

    List<Pedido> findByEstado(String estado);

    List<Pedido> findByIdUsuarioOrderByFechaPedidoDesc(Long idUsuario);
}