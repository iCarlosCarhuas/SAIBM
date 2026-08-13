package edu.pe.cibertec.saibm.libro.domain.exception;
import java.util.UUID;
public class BookNotFoundException extends RuntimeException { public BookNotFoundException(UUID id){super("Book not found: "+id);} }
