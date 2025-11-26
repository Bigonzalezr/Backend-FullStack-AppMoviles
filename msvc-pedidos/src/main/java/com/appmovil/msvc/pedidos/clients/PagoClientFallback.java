package com.appmovil.msvc.pedidos.clients;

import com.appmovil.msvc.pedidos.model.Pago;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PagoClientFallback implements PagoClientRest {

    @Override
    public Pago registrarPago(Pago pago) {
        log.warn("Fallback activado para PagoClient.registrarPago(). El servicio de pagos no está disponible.");
        Pago fallback = new Pago();
        fallback.setMonto(0);
        fallback.setEstado("PENDIENTE");
        return fallback;
    }

    @Override
    public Pago findById(Long id) {
        log.warn("Fallback activado para PagoClient.findById({}). El servicio de pagos no está disponible.", id);
        Pago fallback = new Pago();
        fallback.setIdPago(id);
        fallback.setEstado("NO_DISPONIBLE");
        fallback.setMonto(0);
        return fallback;
    }
}
