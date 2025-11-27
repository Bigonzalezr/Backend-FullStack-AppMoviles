package com.appmovil.msvc.pedidos.assemblers;

import com.appmovil.msvc.pedidos.controller.PedidoController;
import com.appmovil.msvc.pedidos.model.entity.Pedido;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class PedidoModelAssembler implements RepresentationModelAssembler<Pedido, EntityModel<Pedido>> {

    @Override
    @NonNull
    public EntityModel<Pedido> toModel(@NonNull Pedido pedido) {
        return EntityModel.of(
                pedido,
                linkTo(methodOn(PedidoController.class).findById(pedido.getId())).withSelfRel(),
                linkTo(methodOn(PedidoController.class).findAll()).withRel("pedidos"),
                linkTo(methodOn(PedidoController.class).findByUsuario(pedido.getIdUsuario())).withRel("usuario")
        );
    }
}
