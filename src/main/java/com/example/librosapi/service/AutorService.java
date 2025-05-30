package com.example.librosapi.service;


import com.example.librosapi.model.Autor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


public interface AutorService {
    public List<Autor> listarTodos();
    public Optional<Autor> obtenerPorId(Long id);
    public Autor crearAutor(Autor autor);
    public Autor actualizarAutor(Long id, Autor autorDetalles);
    public boolean eliminarAutor(Long id);
    public boolean existeAutor(Long id);
}
