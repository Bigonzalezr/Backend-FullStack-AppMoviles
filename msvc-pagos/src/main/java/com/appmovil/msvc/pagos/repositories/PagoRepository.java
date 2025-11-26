package com.appmovil.msvc.pagos.repositories;

import com.appmovil.msvc.pagos.models.entities.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByIdPedido(Long idPedido);

    List<Pago> findByIdUsuario(Long idUsuario);

    List<Pago> findByEstado(String estado);

    List<Pago> findByMetodoPago(String metodoPago);

    List<Pago> findByIdUsuarioAndEstado(Long idUsuario, String estado);

    List<Pago> findByFechaPagoBetween(LocalDateTime inicio, LocalDateTime fin);

    Optional<Pago> findByNumeroTransaccion(String numeroTransaccion);

    @Query("SELECT p FROM Pago p WHERE p.idUsuario = :idUsuario ORDER BY p.fechaPago DESC")
    List<Pago> findRecentPaymentsByUsuario(@Param("idUsuario") Long idUsuario);
}
