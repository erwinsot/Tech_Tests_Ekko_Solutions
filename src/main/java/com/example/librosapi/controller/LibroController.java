package com.example.librosapi.controller;


import com.example.librosapi.model.Libro;
import com.example.librosapi.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/libros")
public class LibroController {


    private LibroService libroService;

    @Autowired
    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping
    public List<Libro> listarLibros() {
        return libroService.listarTodos();
    }



    @GetMapping("/autor/{autorId}")
    public List<Libro> listarLibrosPorAutor(@PathVariable Long autorId) {
        return libroService.listarPorAutor(autorId);
    }

    @PostMapping
    public Libro crearLibro(@RequestBody Libro libro) {
        return libroService.crearLibro(libro);
    }

    @PutMapping()
    public ResponseEntity<Libro> actualizarLibro(@RequestBody Libro libro) {
        System.out.println("Libro actualizado: " + libro);
        return ResponseEntity.ok(libroService.actualizarLibro(libro));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLibro(@PathVariable Long id) {
        libroService.eliminarLibro(id);
        return ResponseEntity.ok().build();
    }
}
