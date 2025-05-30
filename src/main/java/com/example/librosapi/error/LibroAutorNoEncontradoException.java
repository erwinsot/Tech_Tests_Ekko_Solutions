package com.example.librosapi.error;

public class LibroAutorNoEncontradoException  extends RuntimeException {
    public LibroAutorNoEncontradoException(Long id) {
        super("El autor "+ id + " asociado al libro no existe");
    }
}
