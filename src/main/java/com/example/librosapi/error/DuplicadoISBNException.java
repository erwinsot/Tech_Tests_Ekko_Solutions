package com.example.librosapi.error;

public class DuplicadoISBNException extends RuntimeException {
    public DuplicadoISBNException(String isbn) {
        super("Ya existe un libro con el ISBN: " + isbn);
    }
}

