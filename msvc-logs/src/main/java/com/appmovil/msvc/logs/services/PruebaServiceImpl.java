package com.appmovil.msvc.prueba.services;


import com.appmovil.msvc.prueba.clients.CursosClientRest;
import com.appmovil.msvc.prueba.clients.ProfesorClientRest;
import com.appmovil.msvc.prueba.clients.PruebaClientRest;
import com.appmovil.msvc.prueba.dtos.CursoDTO;
import com.appmovil.msvc.prueba.dtos.ProfesorDTO;
import com.appmovil.msvc.prueba.dtos.PruebaDTO;
import com.appmovil.msvc.prueba.exceptions.PruebaException;
import com.appmovil.msvc.prueba.models.Cursos;
import com.appmovil.msvc.prueba.models.Profesores;
import com.appmovil.msvc.prueba.models.entities.Prueba;
import com.appmovil.msvc.prueba.repositories.PruebaRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PruebaServiceImpl implements PruebaService {

    @Autowired
    private PruebaRepository pruebaRepository;

    @Autowired
    private CursosClientRest cursoClientRest;

    @Autowired
    private ProfesorClientRest profesorClientRest;


    @Override
    public List<PruebaDTO> findAll() {
        return this.pruebaRepository.findAll().stream().map(Prueba ->{
            Prueba prueba = null;

            prueba = this.pruebaRepository.findById(prueba.getIdPrueba()).orElseThrow(
                    () -> new PruebaException("ALGO")
            );

            Cursos curso = null;
            try{
                curso = this.cursoClientRest.findById(prueba.getIdCurso());
            }catch (FeignException ex){
                throw new PruebaException("No se encontro el curso");
            }

            Profesores profesor = null;
            try{
                profesor = this.profesorClientRest.findById(prueba.getIdProfesor());
            }catch (FeignException ex){
                throw new PruebaException("No se encontro el profesor");
            }

            CursoDTO cursoDTO = new CursoDTO();
            cursoDTO.setComentario(curso.getComentario());
            cursoDTO.setDuracion(curso.getDuracion());
            cursoDTO.setFechaCreacion(curso.getFechaCreacion());
            cursoDTO.setPrecio(curso.getPrecio());
            cursoDTO.setEstado(curso.getEstado());

            ProfesorDTO profesorDTO = new ProfesorDTO();
            profesorDTO.setRun(profesorDTO.getRun());
            profesorDTO.setNombres(profesorDTO.getNombres());
            profesorDTO.setApellidos(profesorDTO.getApellidos());
            profesorDTO.setFechaNacimiento(profesorDTO.getFechaNacimiento());
            profesorDTO.setCorreo(profesorDTO.getCorreo());
            profesorDTO.setContrasenia(profesorDTO.getContrasenia());
            profesorDTO.setCuentaActiva(profesorDTO.getCuentaActiva());

            PruebaDTO pruebaDTO = new PruebaDTO();
            pruebaDTO.setCurso(cursoDTO);
            pruebaDTO.setProfesor(profesorDTO);
            return pruebaDTO;
        }).toList();
    }

    @Override
    public Prueba findById(Long id){
        return this.pruebaRepository.findById(id).orElseThrow(
                () -> new PruebaException("La prueba con id "+id+"no se encuentra disponible")
        );
    }

    @Override
    public Prueba save (Prueba prueba) {
        try {
            Cursos curso = this.cursoClientRest.findById(prueba.getIdCurso());
            Profesores profesor = this.profesorClientRest.findById(prueba.getIdProfesor());
        } catch (FeignException ex) {
            throw new PruebaException("Existen problemas con la asosiacion Profesor Curso");
        }
        return this.pruebaRepository.save(prueba);
    }

     @Override
    public List<Prueba> findByIdProfesor(Long profesorId){
        return this.pruebaRepository.findByIdProfesor(profesorId);
     }
     @Override
    public List<Prueba> findByIdCurso(Long cursoId){
        return this.pruebaRepository.findByIdCurso(cursoId);
     }
}
