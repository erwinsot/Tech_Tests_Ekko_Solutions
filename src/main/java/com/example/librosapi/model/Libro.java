package com.example.librosapi.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "libros")
public class Libro {
    @Id
    @Column(name = "libro_id")
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(unique = true)
    private String isbn;

    @Column(name = "fecha_publicacion")
    private Date fechaPublicacion;

    @ManyToOne
    @JoinColumn(name = "autor_id", nullable = false)
    private Autor autor;
}