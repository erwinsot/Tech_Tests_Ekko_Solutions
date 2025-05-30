package com.example.librosapi.service.impl;


import com.example.librosapi.error.AutorNoEncontradoException;
import com.example.librosapi.error.DuplicadoISBNException;
import com.example.librosapi.error.LibroAutorNoEncontradoException;
import com.example.librosapi.error.LibroNoEncontradoException;
import com.example.librosapi.model.Libro;
import com.example.librosapi.repository.LibroRepository;
import com.example.librosapi.service.AutorService;
import com.example.librosapi.service.LibroService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LibroServiceImpl implements LibroService {
    private final LibroRepository libroRepository;
    private final AutorService autorService;
    private final EntityManager entityManager;

    @Autowired
    public LibroServiceImpl(LibroRepository libroRepository,
                        AutorService autorService,
                        EntityManager entityManager) {
        this.libroRepository = libroRepository;
        this.autorService = autorService;
        this.entityManager = entityManager;
    }

    /**
     * Obtiene todos los libros
     * @return Lista de libros
     */
    public List<Libro> listarTodos() {
        return libroRepository.findAll();
    }

    /**
     * Busca un libro por su ID
     * @param id ID del libro
     * @return Optional con el libro si existe
     */
    public Optional<Libro> obtenerPorId(Long id) {
        return libroRepository.findById(id);
    }

    /**
     * Obtiene libros por autor
     * @param autorId ID del autor
     * @return Lista de libros del autor
     */
    public List<Libro> listarPorAutor(Long autorId) {
        return libroRepository.findByAutorId(autorId);
    }

    /**
     * Crea un nuevo libro usando el procedimiento almacenado PL/SQL
     * @param libro Datos del libro
     * @return Libro creado
     * @throws RuntimeException Si el autor no existe
     */
    @Transactional
    public Libro crearLibro(Libro libro) {
        // Validar que el autor existe
        if (!autorService.existeAutor(libro.getAutor().getId())) {
            throw new AutorNoEncontradoException(libro.getAutor().getId());
        }
        try {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("libros_pkg.insertar_libro")
                .registerStoredProcedureParameter("p_titulo", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_isbn", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_fecha_publicacion", java.sql.Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_autor_id", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_libro_id", Integer.class, ParameterMode.OUT)
                .setParameter("p_titulo", libro.getTitulo())
                .setParameter("p_isbn", libro.getIsbn())
                .setParameter("p_autor_id", libro.getAutor().getId());

        if (libro.getFechaPublicacion() != null) {
            query.setParameter("p_fecha_publicacion",
                    new java.sql.Date(libro.getFechaPublicacion().getTime()));
        } else {
            query.setParameter("p_fecha_publicacion", null);
        }

        query.execute();

        Integer nuevoId = (Integer) query.getOutputParameterValue("p_libro_id");
        libro.setId(nuevoId.longValue());

        return libro;

    } catch (RuntimeException e) {
        if (e.getMessage().contains("ORA-00001")) {
            throw new DuplicadoISBNException(libro.getIsbn());
        }
        throw e; // relanzar otras excepciones
    }
    }

    @Transactional
    public Libro actualizarLibro( Libro libroDetalles) {
        System.out.println(libroDetalles.getTitulo() + " " + libroDetalles.getIsbn() + " " + libroDetalles.getFechaPublicacion() + " " + libroDetalles.getAutor().getId());
        return libroRepository.findById(libroDetalles.getId())
                .map(libro -> {
                    // Validar que el nuevo autor existe
                    if (!autorService.existeAutor(libroDetalles.getAutor().getId())) {
                        throw new LibroAutorNoEncontradoException( libroDetalles.getAutor().getId());
                    }

                    libro.setTitulo(libroDetalles.getTitulo());
                    libro.setIsbn(libroDetalles.getIsbn());
                    libro.setFechaPublicacion(libroDetalles.getFechaPublicacion());
                    libro.setAutor(libroDetalles.getAutor());

                    // Llamar al procedimiento almacenado de actualización
                    StoredProcedureQuery query = entityManager.createStoredProcedureQuery("libros_pkg.actualizar_libro")
                            .registerStoredProcedureParameter("p_libro_id", Integer.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("p_titulo", String.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("p_isbn", String.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("p_fecha_publicacion", java.sql.Date.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("p_autor_id", Integer.class, ParameterMode.IN)
                            .setParameter("p_libro_id", libroDetalles.getId().intValue())
                            .setParameter("p_titulo", libroDetalles.getTitulo())
                            .setParameter("p_isbn", libroDetalles.getIsbn())
                            .setParameter("p_autor_id", libroDetalles.getAutor().getId());

                    if (libroDetalles.getFechaPublicacion() != null) {
                        query.setParameter("p_fecha_publicacion",
                                new java.sql.Date(libroDetalles.getFechaPublicacion().getTime()));
                    } else {
                        query.setParameter("p_fecha_publicacion", null);
                    }

                    query.execute();

                    return libroRepository.save(libro);
                })
                .orElseThrow(() -> { throw new LibroNoEncontradoException(libroDetalles.getId());});
    }

    /**
     * Elimina un libro
     * @param id ID del libro a eliminar
     */
    @Transactional
    public void eliminarLibro(Long id) {
        // Verificar que el libro existe
        if (!libroRepository.existsById(id)) {
            throw new LibroNoEncontradoException(id);
        }

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("libros_pkg.eliminar_libro")
                .registerStoredProcedureParameter("p_libro_id", Integer.class, ParameterMode.IN)
                .setParameter("p_libro_id", id.intValue());

        query.execute();
        libroRepository.deleteById(id);
    }
}
