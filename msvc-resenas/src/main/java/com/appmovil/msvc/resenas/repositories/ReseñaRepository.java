package com.appmovil.msvc.resenas.repositories;

import com.appmovil.msvc.resenas.models.entities.Reseña;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReseñaRepository extends JpaRepository<Reseña, Long> {
    List<Reseña> findByIdUsuario(Long idUsuario);
    
    List<Reseña> findByIdProducto(Long idProducto);
    
    List<Reseña> findByIdProductoAndActivo(Long idProducto, Boolean activo);
    
    @Query("SELECT AVG(r.rating) FROM Reseña r WHERE r.idProducto = :idProducto AND r.activo = true")
    Double findAverageRatingByProducto(Long idProducto);
}
