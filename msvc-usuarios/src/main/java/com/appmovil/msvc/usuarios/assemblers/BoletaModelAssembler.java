package com.appmovil.msvc.boleta.assemblers;

import com.appmovil.msvc.boleta.controllers.BoletaController;
import com.appmovil.msvc.boleta.models.entities.Boleta;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class BoletaModelAssembler implements RepresentationModelAssembler<Boleta, EntityModel<Boleta>> {

    @Override
    public EntityModel<Boleta> toModel(Boleta boleta) {
        return EntityModel.of(
                boleta,
                linkTo(methodOn(BoletaController.class).findById(boleta.getIdBoleta())).withSelfRel(),
                linkTo(methodOn(BoletaController.class).findAll()).withRel("boletas"),
                linkTo(methodOn(BoletaController.class).findByAlumnoId(boleta.getIdAlumno())).withRel("alumno"),
                linkTo(methodOn(BoletaController.class).findByProfesorId(boleta.getIdProfesor())).withRel("profesor")
        );
    }
}
