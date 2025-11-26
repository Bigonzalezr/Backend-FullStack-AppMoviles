package com.appmovil.msvc.resenas.services;

import com.appmovil.msvc.resenas.clients.UsuarioClientRest;
import com.appmovil.msvc.resenas.clients.ProductoClientRest;
import com.appmovil.msvc.resenas.dtos.ResenaDTO;
import com.appmovil.msvc.resenas.exceptions.ResenaException;
import com.appmovil.msvc.resenas.models.Usuario;
import com.appmovil.msvc.resenas.models.Producto;
import com.appmovil.msvc.resenas.models.entities.Resena;
import com.appmovil.msvc.resenas.repositories.ResenaRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResenaServiceImpl implements ResenaService {

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private UsuarioClientRest usuarioClientRest;

    @Autowired
    private ProductoClientRest productoClientRest;

    private ResenaDTO convertToDTO(Resena resena) {
        Usuario usuario = null;
        try {
            usuario = this.usuarioClientRest.findById(resena.getIdUsuario());
        } catch (FeignException e) {
            throw new ResenaException("El usuario no existe");
        }

        Producto producto = null;
        try {
            producto = this.productoClientRest.findById(resena.getIdProducto());
        } catch (FeignException e) {
            throw new ResenaException("El producto no existe");
        }

        return ResenaDTO.builder()
                .id(resena.getId())
                .idUsuario(resena.getIdUsuario())
                .nombreUsuario(usuario.getNombre())
                .idProducto(resena.getIdProducto())
                .nombreProducto(producto.getNombre())
                .rating(resena.getRating())
                .comentario(resena.getComentario())
                .fechaCreacion(resena.getFechaCreacion())
                .activo(resena.getActivo())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResenaDTO> findAll() {
        return this.resenaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ResenaDTO findById(Long id) {
        Resena Resena = this.resenaRepository.findById(id).orElseThrow(
                () -> new ResenaException("Resena con id " + id + " no encontrada")
        );
        return convertToDTO(Resena);
    }

    @Override
    @Transactional
    public ResenaDTO save(Resena resena) {
        try {
            this.usuarioClientRest.findById(resena.getIdUsuario());
        } catch (FeignException ex) {
            throw new ResenaException("No se pudo guardar la Resena: Usuario no existe");
        }
        
        try {
            this.productoClientRest.findById(resena.getIdProducto());
        } catch (FeignException ex) {
            throw new ResenaException("No se pudo guardar la Resena: Producto no existe");
        }
        
        // Validar que no exista una Resena previa del mismo usuario para este producto
        if (this.resenaRepository.existsByIdUsuarioAndIdProducto(resena.getIdUsuario(), resena.getIdProducto())) {
            throw new ResenaException("Ya existe una Resena de este usuario para este producto");
        }
        
        Resena saved = this.resenaRepository.save(resena);
        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public ResenaDTO update(Long id, Resena resena) {
        Resena existing = this.resenaRepository.findById(id).orElseThrow(
                () -> new ResenaException("Resena con id " + id + " no encontrada")
        );
        
        existing.setRating(resena.getRating());
        existing.setComentario(resena.getComentario());
        
        Resena updated = this.resenaRepository.save(existing);
        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Resena resena = this.resenaRepository.findById(id).orElseThrow(
                () -> new ResenaException("Resena con id " + id + " no encontrada")
        );
        resena.setActivo(false);
        this.resenaRepository.save(resena);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResenaDTO> findByUsuario(Long idUsuario) {
        return this.resenaRepository.findByIdUsuario(idUsuario).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResenaDTO> findByProducto(Long idProducto) {
        return this.resenaRepository.findByIdProductoAndActivo(idProducto, true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRatingByProducto(Long idProducto) {
        Double avg = this.resenaRepository.findAverageRatingByProducto(idProducto);
        return avg != null ? avg : 0.0;
    }
}
