package com.edutech.msvc.inscripcion.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
public class InscripcionDTO {

    private LocalDate fechaInscripcion;
    private CursoDTO curso;
    private AlumnoDTO alumno;
}
