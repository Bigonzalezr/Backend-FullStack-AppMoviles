package com.appmovil.msvc.resenas.services;

import com.appmovil.msvc.resenas.clients.UsuarioClientRest;
import com.appmovil.msvc.resenas.clients.ProductoClientRest;
import com.appmovil.msvc.resenas.dtos.ReseñaDTO;
import com.appmovil.msvc.resenas.exceptions.ReseñaException;
import com.appmovil.msvc.resenas.models.Usuario;
import com.appmovil.msvc.resenas.models.Producto;
import com.appmovil.msvc.resenas.models.entities.Reseña;
import com.appmovil.msvc.resenas.repositories.ReseñaRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReseñaServiceImpl implements ReseñaService {

    @Autowired
    private ReseñaRepository reseñaRepository;

    @Autowired
    private UsuarioClientRest usuarioClientRest;

    @Autowired
    private ProductoClientRest productoClientRest;

    private ReseñaDTO convertToDTO(Reseña reseña) {
        Usuario usuario = null;
        try {
            usuario = this.usuarioClientRest.findById(reseña.getIdUsuario());
        } catch (FeignException e) {
            throw new ReseñaException("El usuario no existe");
        }

        Producto producto = null;
        try {
            producto = this.productoClientRest.findById(reseña.getIdProducto());
        } catch (FeignException e) {
            throw new ReseñaException("El producto no existe");
        }

        return ReseñaDTO.builder()
                .id(reseña.getId())
                .idUsuario(reseña.getIdUsuario())
                .nombreUsuario(usuario.getNombre())
                .idProducto(reseña.getIdProducto())
                .nombreProducto(producto.getNombre())
                .rating(reseña.getRating())
                .comentario(reseña.getComentario())
                .fechaCreacion(reseña.getFechaCreacion())
                .activo(reseña.getActivo())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReseñaDTO> findAll() {
        return this.reseñaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReseñaDTO findById(Long id) {
        Reseña reseña = this.reseñaRepository.findById(id).orElseThrow(
                () -> new ReseñaException("Reseña con id " + id + " no encontrada")
        );
        return convertToDTO(reseña);
    }

    @Override
    @Transactional
    public ReseñaDTO save(Reseña reseña) {
        try {
            this.usuarioClientRest.findById(reseña.getIdUsuario());
        } catch (FeignException ex) {
            throw new ReseñaException("No se pudo guardar la reseña: Usuario no existe");
        }
        
        try {
            this.productoClientRest.findById(reseña.getIdProducto());
        } catch (FeignException ex) {
            throw new ReseñaException("No se pudo guardar la reseña: Producto no existe");
        }
        
        Reseña saved = this.reseñaRepository.save(reseña);
        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public ReseñaDTO update(Long id, Reseña reseña) {
        Reseña existing = this.reseñaRepository.findById(id).orElseThrow(
                () -> new ReseñaException("Reseña con id " + id + " no encontrada")
        );
        
        existing.setRating(reseña.getRating());
        existing.setComentario(reseña.getComentario());
        
        Reseña updated = this.reseñaRepository.save(existing);
        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Reseña reseña = this.reseñaRepository.findById(id).orElseThrow(
                () -> new ReseñaException("Reseña con id " + id + " no encontrada")
        );
        reseña.setActivo(false);
        this.reseñaRepository.save(reseña);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReseñaDTO> findByUsuario(Long idUsuario) {
        return this.reseñaRepository.findByIdUsuario(idUsuario).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReseñaDTO> findByProducto(Long idProducto) {
        return this.reseñaRepository.findByIdProductoAndActivo(idProducto, true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRatingByProducto(Long idProducto) {
        Double avg = this.reseñaRepository.findAverageRatingByProducto(idProducto);
        return avg != null ? avg : 0.0;
    }
}
