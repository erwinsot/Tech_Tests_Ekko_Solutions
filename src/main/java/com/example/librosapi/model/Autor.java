package com.example.librosapi.model;



import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "autores")

public class Autor {
    @Id
    @Column(name = "autor_id")
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "fecha_nacimiento")
    private Date fechaNacimiento;

    private String nacionalidad;
}
