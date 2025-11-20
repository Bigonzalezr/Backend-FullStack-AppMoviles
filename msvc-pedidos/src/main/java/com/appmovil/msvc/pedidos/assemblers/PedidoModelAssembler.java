package com.appmovil.msvc.pedidos.assemblers;

import com.appmovil.msvc.pedidos.controllers.PedidoController;
import com.appmovil.msvc.pedidos.models.entities.Pedido;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component

public class PedidoModelAssembler implements RepresentationModelAssembler<Pedido, EntityModel<Pedido>> {

    @Override
    public EntityModel<Pedido> toModel(Pedido pedido) {

        return EntityModel.of(
                pedido,
                linkTo(methodOn(PedidoController.class).findById(pedido.getId())).withSelfRel(),
                linkTo(methodOn(PedidoController.class).findAll()).withRel("pedidos"),
                linkTo(methodOn(PedidoController.class).findByIdUsuario(pedido.getIdUsuario())).withRel("usuario")
        );
    }
}