package com.appmovil.msvc.pagos.controllers;

import com.appmovil.msvc.pagos.dtos.PagoDTO;
import com.appmovil.msvc.pagos.dtos.ProcesarPagoDTO;
import com.appmovil.msvc.pagos.dtos.ReembolsoDTO;
import com.appmovil.msvc.pagos.services.PagoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pagos")
@Validated
@Slf4j
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @PostMapping
    public ResponseEntity<PagoDTO> procesarPago(@RequestBody @Valid ProcesarPagoDTO procesarPagoDTO) {
        log.info("Recibida solicitud para procesar pago: {}", procesarPagoDTO);
        PagoDTO pago = pagoService.procesarPago(procesarPagoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(pago);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoDTO> findById(@PathVariable Long id) {
        PagoDTO pago = pagoService.findById(id);
        return ResponseEntity.ok(pago);
    }

    @GetMapping
    public ResponseEntity<List<PagoDTO>> findAll() {
        List<PagoDTO> pagos = pagoService.findAll();
        return ResponseEntity.ok(pagos);
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<PagoDTO>> findByUsuario(@PathVariable Long idUsuario) {
        List<PagoDTO> pagos = pagoService.findByUsuario(idUsuario);
        return ResponseEntity.ok(pagos);
    }

    @GetMapping("/pedido/{idPedido}")
    public ResponseEntity<PagoDTO> findByPedido(@PathVariable Long idPedido) {
        PagoDTO pago = pagoService.findByPedido(idPedido);
        return ResponseEntity.ok(pago);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PagoDTO>> findByEstado(@PathVariable String estado) {
        List<PagoDTO> pagos = pagoService.findByEstado(estado);
        return ResponseEntity.ok(pagos);
    }

    @GetMapping("/metodo-pago/{metodoPago}")
    public ResponseEntity<List<PagoDTO>> findByMetodoPago(@PathVariable String metodoPago) {
        List<PagoDTO> pagos = pagoService.findByMetodoPago(metodoPago);
        return ResponseEntity.ok(pagos);
    }

    @GetMapping("/fecha-rango")
    public ResponseEntity<List<PagoDTO>> findByFechaRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        List<PagoDTO> pagos = pagoService.findByFechaRango(inicio, fin);
        return ResponseEntity.ok(pagos);
    }

    @PatchMapping("/{id}/verificar")
    public ResponseEntity<PagoDTO> verificarPago(@PathVariable Long id) {
        log.info("Verificando estado del pago: {}", id);
        PagoDTO pago = pagoService.verificarPago(id);
        return ResponseEntity.ok(pago);
    }

    @PostMapping("/{id}/reembolsar")
    public ResponseEntity<PagoDTO> reembolsarPago(
            @PathVariable Long id,
            @RequestBody @Valid ReembolsoDTO reembolsoDTO) {
        log.info("Procesando reembolso para pago: {}", id);
        PagoDTO pago = pagoService.reembolsarPago(id, reembolsoDTO.getMotivo());
        return ResponseEntity.ok(pago);
    }
}
