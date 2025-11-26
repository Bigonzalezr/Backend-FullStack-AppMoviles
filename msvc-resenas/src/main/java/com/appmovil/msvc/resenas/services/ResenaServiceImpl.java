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
    private ResenaRepository ResenaRepository;

    @Autowired
    private UsuarioClientRest usuarioClientRest;

    @Autowired
    private ProductoClientRest productoClientRest;

    private ResenaDTO convertToDTO(Resena Resena) {
        Usuario usuario = null;
        try {
            usuario = this.usuarioClientRest.findById(Resena.getIdUsuario());
        } catch (FeignException e) {
            throw new ResenaException("El usuario no existe");
        }

        Producto producto = null;
        try {
            producto = this.productoClientRest.findById(Resena.getIdProducto());
        } catch (FeignException e) {
            throw new ResenaException("El producto no existe");
        }

        return ResenaDTO.builder()
                .id(Resena.getId())
                .idUsuario(Resena.getIdUsuario())
                .nombreUsuario(usuario.getNombre())
                .idProducto(Resena.getIdProducto())
                .nombreProducto(producto.getNombre())
                .rating(Resena.getRating())
                .comentario(Resena.getComentario())
                .fechaCreacion(Resena.getFechaCreacion())
                .activo(Resena.getActivo())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResenaDTO> findAll() {
        return this.ResenaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ResenaDTO findById(Long id) {
        Resena Resena = this.ResenaRepository.findById(id).orElseThrow(
                () -> new ResenaException("Resena con id " + id + " no encontrada")
        );
        return convertToDTO(Resena);
    }

    @Override
    @Transactional
    public ResenaDTO save(Resena Resena) {
        try {
            this.usuarioClientRest.findById(Resena.getIdUsuario());
        } catch (FeignException ex) {
            throw new ResenaException("No se pudo guardar la Resena: Usuario no existe");
        }
        
        try {
            this.productoClientRest.findById(Resena.getIdProducto());
        } catch (FeignException ex) {
            throw new ResenaException("No se pudo guardar la Resena: Producto no existe");
        }
        
        // Validar que no exista una Resena previa del mismo usuario para este producto
        if (this.ResenaRepository.existsByIdUsuarioAndIdProducto(Resena.getIdUsuario(), Resena.getIdProducto())) {
            throw new ResenaException("Ya existe una Resena de este usuario para este producto");
        }
        
        Resena saved = this.ResenaRepository.save(Resena);
        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public ResenaDTO update(Long id, Resena Resena) {
        Resena existing = this.ResenaRepository.findById(id).orElseThrow(
                () -> new ResenaException("Resena con id " + id + " no encontrada")
        );
        
        existing.setRating(Resena.getRating());
        existing.setComentario(Resena.getComentario());
        
        Resena updated = this.ResenaRepository.save(existing);
        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Resena Resena = this.ResenaRepository.findById(id).orElseThrow(
                () -> new ResenaException("Resena con id " + id + " no encontrada")
        );
        Resena.setActivo(false);
        this.ResenaRepository.save(Resena);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResenaDTO> findByUsuario(Long idUsuario) {
        return this.ResenaRepository.findByIdUsuario(idUsuario).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResenaDTO> findByProducto(Long idProducto) {
        return this.ResenaRepository.findByIdProductoAndActivo(idProducto, true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRatingByProducto(Long idProducto) {
        Double avg = this.ResenaRepository.findAverageRatingByProducto(idProducto);
        return avg != null ? avg : 0.0;
    }
}
