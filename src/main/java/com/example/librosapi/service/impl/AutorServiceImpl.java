package com.example.librosapi.service.impl;


import com.example.librosapi.error.AutorNoEncontradoException;
import com.example.librosapi.model.Autor;
import com.example.librosapi.repository.AutorRepository;
import com.example.librosapi.service.AutorService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AutorServiceImpl implements AutorService {

    private final AutorRepository autorRepository;
    private final EntityManager entityManager;

    @Autowired
    public AutorServiceImpl(AutorRepository autorRepository, EntityManager entityManager) {
        this.autorRepository = autorRepository;
        this.entityManager = entityManager;
    }

    /**
     * Obtiene todos los autores utilizando el procedimiento almacenado PL/SQL
     * @return Lista de autores
     */
    public List<Autor> listarTodos() {
        return autorRepository.findAll();
    }

    /**
     * Busca un autor por su ID
     * @param id ID del autor
     * @return Optional con el autor si existe
     */
    public Optional<Autor> obtenerPorId(Long id) {
        return autorRepository.findById(id);
    }

    /**
     * Crea un nuevo autor utilizando el procedimiento almacenado PL/SQL
     * @param autor Datos del autor a crear
     * @return Autor creado con su ID asignado
     */
    @Transactional
    public Autor crearAutor(Autor autor) {
        // Llamada al procedimiento almacenado de creación
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("libros_pkg.insertar_autor")
                .registerStoredProcedureParameter("p_nombre", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_fecha_nacimiento", java.sql.Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_nacionalidad", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_autor_id", Integer.class, ParameterMode.OUT)
                .setParameter("p_nombre", autor.getNombre())
                .setParameter("p_nacionalidad", autor.getNacionalidad());

        if (autor.getFechaNacimiento() != null) {
            query.setParameter("p_fecha_nacimiento",
                    new java.sql.Date(autor.getFechaNacimiento().getTime()));
        } else {
            query.setParameter("p_fecha_nacimiento", null);
        }

        query.execute();

        // Obtener el ID generado
        Integer idGenerado = (Integer) query.getOutputParameterValue("p_autor_id");
        autor.setId(idGenerado.longValue());

        return autorRepository.save(autor);
    }

    /**
     * Actualiza un autor existente
     * @param id ID del autor a actualizar
     * @param autorDetalles Nuevos datos del autor
     * @return Autor actualizado
     */
    @Transactional
    public Autor actualizarAutor(Long id, Autor autorDetalles) {
        return autorRepository.findById(id)
                .map(autor -> {
                    autor.setNombre(autorDetalles.getNombre());
                    autor.setFechaNacimiento(autorDetalles.getFechaNacimiento());
                    autor.setNacionalidad(autorDetalles.getNacionalidad());

                    // Llamada al procedimiento almacenado de actualización
                    StoredProcedureQuery query = entityManager.createStoredProcedureQuery("libros_pkg.actualizar_autor")
                            .registerStoredProcedureParameter("p_autor_id", Integer.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("p_nombre", String.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("p_fecha_nacimiento", java.sql.Date.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("p_nacionalidad", String.class, ParameterMode.IN)
                            .setParameter("p_autor_id", id.intValue())
                            .setParameter("p_nombre", autorDetalles.getNombre())
                            .setParameter("p_nacionalidad", autorDetalles.getNacionalidad());

                    if (autorDetalles.getFechaNacimiento() != null) {
                        query.setParameter("p_fecha_nacimiento",
                                new java.sql.Date(autorDetalles.getFechaNacimiento().getTime()));
                    } else {
                        query.setParameter("p_fecha_nacimiento", null);
                    }

                    query.execute();

                    return autorRepository.save(autor);
                })
                .orElseThrow(() -> {throw new AutorNoEncontradoException(id);});
    }

    /**
     * Elimina un autor si no tiene libros asociados
     * @param id ID del autor a eliminar
     * @return true si se eliminó, false si no se pudo por tener libros asociados
     */
    @Transactional
    public boolean eliminarAutor(Long id) {
        autorRepository.findById(id).orElseThrow(() -> {throw new AutorNoEncontradoException(id);});
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("libros_pkg.eliminar_autor")
                .registerStoredProcedureParameter("p_autor_id", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_resultado", Integer.class, ParameterMode.OUT)
                .setParameter("p_autor_id", id.intValue());

        query.execute();

        Integer resultado = (Integer) query.getOutputParameterValue("p_resultado");

        if (resultado == 1) {
            autorRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Verifica si un autor existe
     * @param id ID del autor
     * @return true si existe, false si no
     */
    public boolean existeAutor(Long id) {
        return autorRepository.existsById(id);
    }
}
