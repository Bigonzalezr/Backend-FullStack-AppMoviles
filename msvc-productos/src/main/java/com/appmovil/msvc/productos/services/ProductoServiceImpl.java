package com.appmovil.msvc.productos.services;

import com.appmovil.msvc.productos.dtos.ProductoUpdateDTO;
import com.appmovil.msvc.productos.exception.ProductoException;
import com.appmovil.msvc.productos.models.entities.Producto;
import com.appmovil.msvc.productos.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProductoServiceImpl implements ProductoService {


 @Autowired
 private ProductoRepository productoRepository;



 @Override
 @Transactional(readOnly = true)
 public List<Producto> findAll() {
  return this.productoRepository.findAll();
 }

 @Override
 @Transactional(readOnly = true)
 public Producto findById(Long id) {

  return this.productoRepository.findById(id).orElseThrow(
          () -> new ProductoException("El Producto con ID " + id + " no se encuentra en la base de datos")
  );
 }

 @Override
 @Transactional
 public Producto save(Producto producto) {

  return this.productoRepository.save(producto);
 }

 @Override
 @Transactional
 public Producto update(Long id, ProductoUpdateDTO updateDTO) {

  Producto productoDB = this.productoRepository.findById(id).orElseThrow(
          () -> new ProductoException("Producto con ID " + id + " no encontrado para actualizar")
  );


  productoDB.setNombre(updateDTO.getNombre());
  productoDB.setPrecio(updateDTO.getPrecio());
  productoDB.setCategoria(updateDTO.getCategoria());
  productoDB.setImagen(updateDTO.getImagen());
  productoDB.setDescripcion(updateDTO.getDescripcion());
  productoDB.setStock(updateDTO.getStock());

  if (updateDTO.getRating() != null) {
   productoDB.setRating(updateDTO.getRating());
  }


  return this.productoRepository.save(productoDB);
 }


 @Override
 @Transactional
 public void delete(Long id) {
  if (!productoRepository.existsById(id)) {
   throw new ProductoException("Producto con ID " + id + " no existe para eliminar");
  }
  productoRepository.deleteById(id);
 }

 @Override
 @Transactional(readOnly = true)
 public List<Producto> findByCategoria(String categoria) {
  if ("todos".equalsIgnoreCase(categoria)) {
   return findAll();
  }

  return productoRepository.findByCategoriaIgnoreCase(categoria);
 }
}