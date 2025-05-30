package com.example.librosapi.service;

import com.example.librosapi.model.Libro;

import java.util.List;
import java.util.Optional;

public interface LibroService {
    public List<Libro> listarTodos();
    public List<Libro> listarPorAutor(Long autorId);
    public Libro crearLibro(Libro libro);
    public Libro actualizarLibro(Libro libroDetalles);
    public void eliminarLibro(Long id);

}
