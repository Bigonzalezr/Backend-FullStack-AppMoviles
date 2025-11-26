package com.appmovil.msvc.resenas.repositories;

import com.appmovil.msvc.resenas.models.entities.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByIdUsuario(Long idUsuario);
    
    List<Resena> findByIdProducto(Long idProducto);
    
    List<Resena> findByIdProductoAndActivo(Long idProducto, Boolean activo);
    
    @Query("SELECT AVG(r.rating) FROM Resena r WHERE r.idProducto = :idProducto AND r.activo = true")
    Double findAverageRatingByProducto(Long idProducto);
    
    boolean existsByIdUsuarioAndIdProducto(Long idUsuario, Long idProducto);
}
