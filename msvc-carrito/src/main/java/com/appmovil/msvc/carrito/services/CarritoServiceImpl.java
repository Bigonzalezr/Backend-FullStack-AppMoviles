package com.appmovil.msvc.carrito.services;

import com.appmovil.msvc.carrito.clients.ProductoClientRest;
import com.appmovil.msvc.carrito.clients.UsuarioClientRest;
import com.appmovil.msvc.carrito.dtos.AgregarItemDTO;
import com.appmovil.msvc.carrito.dtos.CarritoDTO;
import com.appmovil.msvc.carrito.dtos.ItemCarritoDTO;
import com.appmovil.msvc.carrito.exceptions.CarritoException;
import com.appmovil.msvc.carrito.exceptions.ResourceNotFoundException;
import com.appmovil.msvc.carrito.models.Producto;
import com.appmovil.msvc.carrito.models.Usuario;
import com.appmovil.msvc.carrito.models.entities.Carrito;
import com.appmovil.msvc.carrito.models.entities.ItemCarrito;
import com.appmovil.msvc.carrito.repositories.CarritoRepository;
import com.appmovil.msvc.carrito.repositories.ItemCarritoRepository;
import feign.FeignException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CarritoServiceImpl implements CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ItemCarritoRepository itemCarritoRepository;

    @Autowired
    private ProductoClientRest productoClientRest;

    @Autowired
    private UsuarioClientRest usuarioClientRest;

    @Override
    public Carrito obtenerCarritoActivo(Long idUsuario) {
        return carritoRepository.findByIdUsuarioAndEstado(idUsuario, "ACTIVO")
                .orElseGet(() -> crearCarrito(idUsuario));
    }

    @Override
    public CarritoDTO obtenerCarritoConDetalles(Long idUsuario) {
        Carrito carrito = obtenerCarritoActivo(idUsuario);
        
        Usuario usuario = null;
        try {
            usuario = usuarioClientRest.findById(idUsuario);
        } catch (FeignException ex) {
            throw new ResourceNotFoundException("Usuario", "id", idUsuario.toString());
        }

        List<ItemCarritoDTO> itemsDTO = carrito.getItems().stream()
                .map(item -> {
                    Producto producto = null;
                    try {
                        producto = productoClientRest.findById(item.getIdProducto());
                    } catch (FeignException ex) {
                        throw new CarritoException("Producto no encontrado: " + item.getIdProducto());
                    }

                    return ItemCarritoDTO.builder()
                            .idItem(item.getIdItem())
                            .idProducto(item.getIdProducto())
                            .nombreProducto(producto.getNombre())
                            .imagenProducto(producto.getImagen())
                            .precioUnitario(item.getPrecioUnitario())
                            .cantidad(item.getCantidad())
                            .subtotal(item.getSubtotal())
                            .build();
                }).toList();

        return CarritoDTO.builder()
                .idCarrito(carrito.getIdCarrito())
                .idUsuario(carrito.getIdUsuario())
                .nombreUsuario(usuario.getNombre() + " " + usuario.getApellido())
                .items(itemsDTO)
                .total(carrito.getTotal())
                .estado(carrito.getEstado())
                .build();
    }

    @Override
    public Carrito agregarItem(Long idUsuario, AgregarItemDTO agregarItemDTO) {
        Carrito carrito = obtenerCarritoActivo(idUsuario);
        
        Producto producto = null;
        try {
            producto = productoClientRest.findById(agregarItemDTO.getIdProducto());
        } catch (FeignException ex) {
            throw new ResourceNotFoundException("Producto", "id", agregarItemDTO.getIdProducto().toString());
        }

        if (!producto.getActivo()) {
            throw new CarritoException("El producto no está disponible");
        }

        if (producto.getStock() < agregarItemDTO.getCantidad()) {
            throw new CarritoException("Stock insuficiente. Disponible: " + producto.getStock());
        }

        Optional<ItemCarrito> itemExistente = itemCarritoRepository
                .findByCarritoIdCarritoAndIdProducto(carrito.getIdCarrito(), agregarItemDTO.getIdProducto());

        if (itemExistente.isPresent()) {
            ItemCarrito item = itemExistente.get();
            int nuevaCantidad = item.getCantidad() + agregarItemDTO.getCantidad();
            
            if (producto.getStock() < nuevaCantidad) {
                throw new CarritoException("Stock insuficiente. Disponible: " + producto.getStock());
            }
            
            item.setCantidad(nuevaCantidad);
            item.calcularSubtotal();
            itemCarritoRepository.save(item);
        } else {
            ItemCarrito nuevoItem = ItemCarrito.builder()
                    .carrito(carrito)
                    .idProducto(agregarItemDTO.getIdProducto())
                    .cantidad(agregarItemDTO.getCantidad())
                    .precioUnitario(producto.getPrecio())
                    .build();
            nuevoItem.calcularSubtotal();
            carrito.agregarItem(nuevoItem);
        }

        carrito.calcularTotal();
        return carritoRepository.save(carrito);
    }

    @Override
    public Carrito actualizarCantidadItem(Long idUsuario, Long idProducto, Integer cantidad) {
        Carrito carrito = obtenerCarritoActivo(idUsuario);
        
        ItemCarrito item = itemCarritoRepository
                .findByCarritoIdCarritoAndIdProducto(carrito.getIdCarrito(), idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "idProducto", idProducto.toString()));

        Producto producto = null;
        try {
            producto = productoClientRest.findById(idProducto);
        } catch (FeignException ex) {
            throw new ResourceNotFoundException("Producto", "id", idProducto.toString());
        }

        if (producto.getStock() < cantidad) {
            throw new CarritoException("Stock insuficiente. Disponible: " + producto.getStock());
        }

        item.setCantidad(cantidad);
        item.calcularSubtotal();
        itemCarritoRepository.save(item);

        carrito.calcularTotal();
        return carritoRepository.save(carrito);
    }

    @Override
    public Carrito removerItem(Long idUsuario, Long idProducto) {
        Carrito carrito = obtenerCarritoActivo(idUsuario);
        
        ItemCarrito item = itemCarritoRepository
                .findByCarritoIdCarritoAndIdProducto(carrito.getIdCarrito(), idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "idProducto", idProducto.toString()));

        carrito.removerItem(item);
        itemCarritoRepository.delete(item);

        carrito.calcularTotal();
        return carritoRepository.save(carrito);
    }

    @Override
    public void vaciarCarrito(Long idUsuario) {
        Carrito carrito = obtenerCarritoActivo(idUsuario);
        itemCarritoRepository.deleteByCarritoIdCarrito(carrito.getIdCarrito());
        carrito.getItems().clear();
        carrito.setTotal(BigDecimal.ZERO);
        carritoRepository.save(carrito);
    }

    @Override
    public Carrito crearCarrito(Long idUsuario) {
        try {
            usuarioClientRest.findById(idUsuario);
        } catch (FeignException ex) {
            throw new ResourceNotFoundException("Usuario", "id", idUsuario.toString());
        }

        Carrito carrito = Carrito.builder()
                .idUsuario(idUsuario)
                .estado("ACTIVO")
                .total(BigDecimal.ZERO)
                .build();
        
        return carritoRepository.save(carrito);
    }
}
