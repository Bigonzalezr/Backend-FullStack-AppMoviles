package com.appmovil.msvc.pedidos.controller;

import com.appmovil.msvc.pedidos.assemblers.PedidoModelAssembler;
import com.appmovil.msvc.pedidos.dtos.ErrorDTO;
import com.appmovil.msvc.pedidos.model.entities.Pedido;
import com.appmovil.msvc.pedidos.services.PedidoService;
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
@RequestMapping("/api/v2/pedidos")
@Validated
@Tag(
        name = "Pedido API HATEOAS",
        description = "CRUD y gestión de Órdenes de Compra (LevelUp Gamer)"
)
public class PedidoControllerV2 {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoModelAssembler pedidoModelAssembler;

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(
            summary = "Listar todos los pedidos",
            description = "Retorna el listado completo de todas las órdenes registradas."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado completo de pedidos",
                    content = @Content(mediaType = MediaTypes.HAL_JSON_VALUE, schema = @Schema(implementation = Pedido.class))
            )
    })
    public ResponseEntity<CollectionModel<EntityModel<Pedido>>> findAll() {
        // Convierte cada entidad Pedido a EntityModel<Pedido>
        List<EntityModel<Pedido>> entityModels = this.pedidoService.findAll()
                .stream()
                .map(pedidoModelAssembler::toModel)
                .toList();


        CollectionModel<EntityModel<Pedido>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(PedidoControllerV2.class).findAll()).withSelfRel()
        );
        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);
    }

    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(
            summary = "Obtener un pedido por ID",
            description = "Retorna una orden específica a partir de su ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido encontrado",
                    content = @Content(mediaType = MediaTypes.HAL_JSON_VALUE, schema = @Schema(implementation = Pedido.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))
            )
    })
    @Parameters({
            @Parameter(name = "id", description = "ID del pedido", required = true)
    })
    public ResponseEntity<EntityModel<Pedido>> findById(@PathVariable Long id) {
        EntityModel<Pedido> entityModel = this.pedidoModelAssembler.toModel(
                this.pedidoService.findById(id)
        );
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);
    }

    @PostMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(
            summary = "Registrar una nueva pedido",
            description = "Guarda una nueva orden en la base de datos (Ejecución de Checkout)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido creado exitosamente",
                    content = @Content(mediaType = MediaTypes.HAL_JSON_VALUE, schema = @Schema(implementation = Pedido.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Error en recursos relacionados (Usuario/Producto no existe)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos de la orden a registrar (Incluye detalles del pedido)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Pedido.class))
    )
    public ResponseEntity<EntityModel<Pedido>> create(@Valid @RequestBody Pedido pedido) {

        Pedido nuevaPedido = this.pedidoService.save(pedido);


        EntityModel<Pedido> entityModel = this.pedidoModelAssembler.toModel(nuevaPedido);


        return ResponseEntity
                .created(linkTo(methodOn(PedidoControllerV2.class).findById(nuevaPedido.getId())).toUri())
                .body(entityModel);
    }
}