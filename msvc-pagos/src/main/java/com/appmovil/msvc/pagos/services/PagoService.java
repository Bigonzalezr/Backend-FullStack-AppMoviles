package com.appmovil.msvc.pagos.services;

import com.appmovil.msvc.pagos.dtos.PagoDTO;
import com.appmovil.msvc.pagos.dtos.ProcesarPagoDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface PagoService {

    PagoDTO procesarPago(ProcesarPagoDTO procesarPagoDTO);

    PagoDTO verificarPago(Long idPago);

    PagoDTO findById(Long id);

    List<PagoDTO> findAll();

    List<PagoDTO> findByUsuario(Long idUsuario);

    PagoDTO findByPedido(Long idPedido);

    List<PagoDTO> findByEstado(String estado);

    List<PagoDTO> findByMetodoPago(String metodoPago);

    List<PagoDTO> findByFechaRango(LocalDateTime inicio, LocalDateTime fin);

    PagoDTO reembolsarPago(Long idPago, String motivo);
}
