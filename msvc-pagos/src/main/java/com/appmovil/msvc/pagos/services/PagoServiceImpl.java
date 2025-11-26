package com.appmovil.msvc.pagos.services;

import com.appmovil.msvc.pagos.clients.PedidoClientRest;
import com.appmovil.msvc.pagos.clients.UsuarioClientRest;
import com.appmovil.msvc.pagos.dtos.PagoDTO;
import com.appmovil.msvc.pagos.dtos.ProcesarPagoDTO;
import com.appmovil.msvc.pagos.exceptions.PagoException;
import com.appmovil.msvc.pagos.exceptions.ResourceNotFoundException;
import com.appmovil.msvc.pagos.models.Pedido;
import com.appmovil.msvc.pagos.models.Usuario;
import com.appmovil.msvc.pagos.models.entities.Pago;
import com.appmovil.msvc.pagos.repositories.PagoRepository;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PagoServiceImpl implements PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private UsuarioClientRest usuarioClientRest;

    @Autowired
    private PedidoClientRest pedidoClientRest;

    @Override
    @Transactional
    public PagoDTO procesarPago(ProcesarPagoDTO procesarPagoDTO) {
        log.info("Procesando pago para pedido: {}", procesarPagoDTO.getIdPedido());

        // Validar que no exista un pago previo para este pedido
        pagoRepository.findByIdPedido(procesarPagoDTO.getIdPedido())
                .ifPresent(p -> {
                    throw new PagoException("Ya existe un pago para este pedido");
                });

        // Validar usuario
        Usuario usuario;
        try {
            usuario = usuarioClientRest.findById(procesarPagoDTO.getIdUsuario());
            if (usuario == null || !usuario.getActivo()) {
                throw new PagoException("El usuario no existe o está inactivo");
            }
        } catch (FeignException ex) {
            throw new PagoException("Error al validar el usuario: " + ex.getMessage());
        }

        // Validar pedido
        Pedido pedido;
        try {
            pedido = pedidoClientRest.findById(procesarPagoDTO.getIdPedido());
            if (pedido == null) {
                throw new PagoException("El pedido no existe");
            }
        } catch (FeignException ex) {
            throw new PagoException("Error al validar el pedido: " + ex.getMessage());
        }

        // Crear el pago
        Pago pago = Pago.builder()
                .idPedido(procesarPagoDTO.getIdPedido())
                .idUsuario(procesarPagoDTO.getIdUsuario())
                .monto(procesarPagoDTO.getMonto())
                .metodoPago(procesarPagoDTO.getMetodoPago())
                .estado("PROCESANDO")
                .fechaPago(LocalDateTime.now())
                .numeroTransaccion(generarNumeroTransaccion())
                .gatewayPago(procesarPagoDTO.getGatewayPago())
                .descripcion(procesarPagoDTO.getDescripcion())
                .build();

        // Simular procesamiento del pago
        try {
            // Aquí iría la integración con el gateway de pago real
            pago.setEstado("COMPLETADO");
            pago.setFechaProcesamiento(LocalDateTime.now());
            pago.setNumeroAutorizacion(UUID.randomUUID().toString().substring(0, 10));
            
            // Si el pago fue exitoso, actualizar estado del pedido
            try {
                pedidoClientRest.actualizarEstadoPago(pedido.getIdPedido());
            } catch (Exception ex) {
                log.warn("No se pudo actualizar el estado del pedido: {}", ex.getMessage());
            }
            
        } catch (Exception ex) {
            pago.setEstado("FALLIDO");
            pago.setMensajeError("Error al procesar el pago: " + ex.getMessage());
            log.error("Error procesando pago: {}", ex.getMessage());
        }

        Pago pagoGuardado = pagoRepository.save(pago);
        return convertirADTO(pagoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoDTO verificarPago(Long idPago) {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id: " + idPago));

        // Aquí iría la verificación con el gateway de pago
        return convertirADTO(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoDTO findById(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id: " + id));
        return convertirADTO(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoDTO> findAll() {
        return pagoRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoDTO> findByUsuario(Long idUsuario) {
        return pagoRepository.findByIdUsuario(idUsuario).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagoDTO findByPedido(Long idPedido) {
        Pago pago = pagoRepository.findByIdPedido(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró pago para el pedido: " + idPedido));
        return convertirADTO(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoDTO> findByEstado(String estado) {
        return pagoRepository.findByEstado(estado).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoDTO> findByMetodoPago(String metodoPago) {
        return pagoRepository.findByMetodoPago(metodoPago).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoDTO> findByFechaRango(LocalDateTime inicio, LocalDateTime fin) {
        return pagoRepository.findByFechaPagoBetween(inicio, fin).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PagoDTO reembolsarPago(Long idPago, String motivo) {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id: " + idPago));

        if (!"COMPLETADO".equals(pago.getEstado())) {
            throw new PagoException("Solo se pueden reembolsar pagos completados");
        }

        // Aquí iría la integración con el gateway para procesar el reembolso
        pago.setEstado("REEMBOLSADO");
        pago.setDescripcion(pago.getDescripcion() + " | Reembolso: " + motivo);
        
        Pago pagoActualizado = pagoRepository.save(pago);
        return convertirADTO(pagoActualizado);
    }

    private PagoDTO convertirADTO(Pago pago) {
        PagoDTO dto = PagoDTO.builder()
                .idPago(pago.getIdPago())
                .idPedido(pago.getIdPedido())
                .idUsuario(pago.getIdUsuario())
                .monto(pago.getMonto())
                .metodoPago(pago.getMetodoPago())
                .estado(pago.getEstado())
                .fechaPago(pago.getFechaPago())
                .fechaProcesamiento(pago.getFechaProcesamiento())
                .numeroTransaccion(pago.getNumeroTransaccion())
                .numeroAutorizacion(pago.getNumeroAutorizacion())
                .gatewayPago(pago.getGatewayPago())
                .ultimosDigitosTarjeta(pago.getUltimosDigitosTarjeta())
                .descripcion(pago.getDescripcion())
                .mensajeError(pago.getMensajeError())
                .build();

        // Enriquecer con datos del usuario
        try {
            Usuario usuario = usuarioClientRest.findById(pago.getIdUsuario());
            if (usuario != null) {
                dto.setNombreUsuario(usuario.getNombre() + " " + usuario.getApellido());
                dto.setEmailUsuario(usuario.getEmail());
            }
        } catch (Exception ex) {
            log.warn("No se pudo obtener información del usuario: {}", ex.getMessage());
        }

        // Enriquecer con datos del pedido
        try {
            Pedido pedido = pedidoClientRest.findById(pago.getIdPedido());
            if (pedido != null) {
                dto.setTotalPedido(pedido.getTotal());
                dto.setEstadoPedido(pedido.getEstado());
            }
        } catch (Exception ex) {
            log.warn("No se pudo obtener información del pedido: {}", ex.getMessage());
        }

        return dto;
    }

    private String generarNumeroTransaccion() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
