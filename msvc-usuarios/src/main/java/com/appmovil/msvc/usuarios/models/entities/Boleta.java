package com.appmovil.msvc.boleta.models.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "boletas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_boleta")
    private Long idBoleta;

    @Column(name = "hora_boleta", nullable = false)
    @NotNull(message = "La hora de la boleta no puede estar vacía")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime horaBoleta;

    @Column(nullable = false)
    @NotNull(message = "El total no puede estar vacío")
    private Integer total;

    @Column(name = "id_alumno", nullable = false)
    @NotNull(message = "Debe existir un ID de alumno")
    private Long idAlumno;

    @Column(name = "id_profesor", nullable = false)
    @NotNull(message = "Debe existir un ID de profesor")
    private Long idProfesor;

    @Column(name = "id_curso", nullable = false)
    @NotNull(message = "Debe existir un ID de curso")
    private Long idCurso;
}
