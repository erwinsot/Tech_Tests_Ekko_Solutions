package com.example.librosapi.error;

public class AutorNoEncontradoException extends RuntimeException {
    public AutorNoEncontradoException(Long id) {
        super("El autor con ID " + id + " no existe");
    }
}
