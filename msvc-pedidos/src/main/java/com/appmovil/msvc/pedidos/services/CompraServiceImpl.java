package com.appmovil.msvc.pedidos.services;

import com.appmovil.msvc.pedidos.dtos.CompraDTO;
import com.appmovil.msvc.pedidos.dtos.AlumnoDTO;
import com.appmovil.msvc.pedidos.dtos.CursoDTO;
import com.appmovil.msvc.pedidos.dtos.ProfesorDTO;
import com.appmovil.msvc.pedidos.exceptions.CompraException;
import com.appmovil.msvc.pedidos.repositories.CompraRepository;
import feign.FeignException;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Data
public class CompraServiceImpl implements com.appmovil.msvc.compra.services.CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private com.edutech.msvc.compra.clients.PagosClientRest pagosClientRest;

    @Autowired
    private com.edutech.msvc.compra.clients.UsuarioClientRest usuarioClientRest;

    @Autowired
    private com.edutech.msvc.compra.clients.ProductosClientRest productosClientRest;

    @Override
    public List<CompraDTO> findAll() {
        return this.compraRepository.findAll().stream().map(compra -> {

            com.edutech.msvc.compra.model.Producto producto;
            try {
                producto = this.pagosClientRest.findById(compra.getIdProfesor());
            } catch (FeignException ex) {
                throw new CompraException("El producto buscado no existe");
            }

            com.edutech.msvc.compra.model.Usuario usuario;
            com.edutech.msvc.compra.model.Pago pago;
            try {
                usuario = this.usuarioClientRest.findById(compra.getIdAlumno());
                pago = this.productosClientRest.findById(compra.getIdCurso());
            } catch (FeignException ex) {
                throw new CompraException("El usuario o el pago no existe en la base de datos");
            }

            ProfesorDTO profesorDTO = new ProfesorDTO();
            profesorDTO.setRunProfesor(producto.getRunProfesor());
            profesorDTO.setNombreCompleto(producto.getNombreCompleto());
            profesorDTO.setFechaNacimiento(producto.getFechaNacimiento());
            profesorDTO.setEstadoCuenta(producto.isEstadoCuenta());

            AlumnoDTO alumnoDTO = new AlumnoDTO();
            alumnoDTO.setRunAlumno(usuario.getRunAlumno());
            alumnoDTO.setNombreCompleto(usuario.getNombreCompleto());
            alumnoDTO.setFechaNacimiento(usuario.getFechaNacimiento());
            alumnoDTO.setEstadoCuenta(usuario.isEstadoCuenta());

            CursoDTO cursoDTO = new CursoDTO();
            cursoDTO.setIdCurso(pago.getIdCurso());
            cursoDTO.setNombreCurso(pago.getNombreCurso());
            cursoDTO.setPrecioCurso(pago.getPrecioCurso());
            cursoDTO.setDescripcionCurso(pago.getDescripcionCurso());

            CompraDTO compraDTO = new CompraDTO();
            compraDTO.setProfesor(profesorDTO);
            compraDTO.setAlumno(alumnoDTO);
            compraDTO.setCurso(cursoDTO);

            return compraDTO;
        }).toList();
    }

    @Override
    public com.edutech.msvc.compra.model.entity.Pedido findById(Long id) {
        return this.compraRepository.findById(id).orElseThrow(
                () -> new CompraException("La compra con id: " + id + " no se encuentra en la base de datos")
        );
    }

    @Override
    public com.edutech.msvc.compra.model.entity.Pedido save(com.edutech.msvc.compra.model.entity.Pedido pedido) {
        try {
            this.usuarioClientRest.findById(pedido.getIdAlumno());
            com.edutech.msvc.compra.model.Pago pago = this.productosClientRest.findById(pedido.getIdCurso());
            this.pagosClientRest.findById(pedido.getIdProfesor());

            int precioTotal = pago.getPrecioCurso() * pedido.getCantidad();
            pedido.setPrecioTotal(precioTotal);

        } catch (FeignException ex) {
            throw new CompraException("Problemas con la asociación profesor, alumno o curso");
        }
        return this.compraRepository.save(pedido);
    }

    @Override
    public List<com.edutech.msvc.compra.model.entity.Pedido> findByProfesorId(Long profesorId) {
        return this.compraRepository.findByIdProfesor(profesorId);
    }

    @Override
    public List<com.edutech.msvc.compra.model.entity.Pedido> findByAlumnoId(Long alumnoId) {
        return this.compraRepository.findByIdAlumno(alumnoId);
    }
}
