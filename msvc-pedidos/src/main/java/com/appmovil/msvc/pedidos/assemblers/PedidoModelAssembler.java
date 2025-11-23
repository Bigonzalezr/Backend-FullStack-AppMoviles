package com.appmovil.msvc.pedidos.assemblers; // Paquete corregido

import com.appmovil.msvc.pedidos.controller.PedidoController;
import com.appmovil.msvc.pedidos.model.entity.Pedido;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class PedidoModelAssembler implements RepresentationModelAssembler<Pedido, EntityModel<Pedido>> {

    @Override
    public EntityModel<Pedido> toModel(Pedido pedido) {

        Long idPrimerProducto = pedido.getDetalles().stream()
                .findFirst()
                .map(PedidoDetalle::getIdProducto)
                .orElse(null);


        EntityModel<Pedido> entityModel = EntityModel.of(
                pedido,

                linkTo(methodOn(PedidoController.class).findById(pedido.getId())).withSelfRel(),


                linkTo(methodOn(PedidoController.class).findAll()).withRel("pedidos"),


                linkTo(methodOn(PedidoController.class).findByIdUsuario(pedido.getIdUsuario())).withRel("usuario")
        );


        if (idPrimerProducto != null) {
            entityModel.add(
                    // Link a /api/pedidos/producto/{idProducto}
                    linkTo(methodOn(PedidoController.class).findByProductoId(idPrimerProducto)).withRel("pedidos-por-producto")
            );
        }

        return entityModel;
    }
}