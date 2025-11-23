package com.appmovil.msvc.boleta.controllers;

import com.appmovil.msvc.boleta.assemblers.BoletaModelAssembler;
import com.appmovil.msvc.boleta.dtos.ErrorDTO;
import com.appmovil.msvc.boleta.models.entities.Boleta;
import com.appmovil.msvc.boleta.services.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@RestController
@RequestMapping("/api/v2/boletas")
@Validated
@Tag(
        name = "Boleta API HATEOAS",
        description = "CRUD para la entidad Boleta"
)
public class BoletaControllerV2 {

    @Autowired
    private BoletaService boletaService;

    @Autowired
    private BoletaModelAssembler boletaModelAssembler;

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Obtener todas las boletas", description = "Devuelve todas las boletas registradas")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de boletas recuperada correctamente",
                    content = @Content(
                            mediaType = MediaTypes.HAL_JSON_VALUE,
                            schema = @Schema(implementation = Boleta.class)
                    )
            )
    })
    public ResponseEntity<CollectionModel<EntityModel<Boleta>>> findAll() {
        List<EntityModel<Boleta>> entityModels = this.boletaService.findAll()
                .stream()
                .map(boletaModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<Boleta>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(BoletaControllerV2.class).findAll()).withSelfRel()
        );
        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);
    }

    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Buscar boleta por ID", description = "Retorna una boleta específica según su ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Boleta encontrada",
                    content = @Content(mediaType = MediaTypes.HAL_JSON_VALUE, schema = @Schema(implementation = Boleta.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Boleta no encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))
            )
    })
    @Parameters({
            @Parameter(name = "id", description = "ID de la boleta", required = true)
    })
    public ResponseEntity<EntityModel<Boleta>> findById(@PathVariable Long id) {
        EntityModel<Boleta> entityModel = this.boletaModelAssembler.toModel(
                this.boletaService.findById(id)
        );
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);
    }

    @PostMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Crear una nueva boleta", description = "Registra una nueva boleta en la base de datos")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Boleta creada con éxito",
                    content = @Content(mediaType = MediaTypes.HAL_JSON_VALUE, schema = @Schema(implementation = Boleta.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Algún recurso relacionado no existe",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La boleta ya existe",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Boleta a registrar",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boleta.class))
    )
    public ResponseEntity<EntityModel<Boleta>> create(@Valid @RequestBody Boleta boleta) {
        Boleta nuevaBoleta = this.boletaService.save(boleta);
        EntityModel<Boleta> entityModel = this.boletaModelAssembler.toModel(nuevaBoleta);
        return ResponseEntity
                .created(linkTo(methodOn(BoletaControllerV2.class).findById(nuevaBoleta.getIdBoleta())).toUri())
                .body(entityModel);
    }
}
